import { NextRequest, NextResponse } from "next/server"
import { trustFlag, rateForCategory, commission, score } from "@/lib/scoring"

type CacheEntry = { at:number; data:any }
const cache = new Map<string, CacheEntry>()
const TTL_MS = 10*60*1000

export async function GET(req: NextRequest){
  const { searchParams } = new URL(req.url)
  const keyword = (searchParams.get("keyword")||"parfum").trim()
  const limit = Math.min(30, Math.max(5, parseInt(searchParams.get("limit")||"20",10)||20))
  if(!keyword) return NextResponse.json({ error:"keyword required" }, {status:400})

  const key = `${keyword.toLowerCase()}:${limit}`
  const hit = cache.get(key)
  if(hit && Date.now()-hit.at < TTL_MS){
    return NextResponse.json({ ...hit.data, cached:true }, { headers:{ "Cache-Control":"public, max-age=60" }})
  }

  try{
    const url = `https://shopee.co.id/api/v4/search/search_items?by=relevancy&keyword=${encodeURIComponent(keyword)}&limit=${limit}&newest=0&order=desc&page_type=search&scenario=PAGE_GLOBAL_SEARCH&version=2`
    // retry 3x
    let lastErr:any=null
    for(let attempt=0; attempt<3; attempt++){
      try{
        if(attempt>0) await new Promise(r=>setTimeout(r, 700*attempt))
        const resp = await fetch(url, {
          headers: {
            "User-Agent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36",
            "Referer": `https://shopee.co.id/search?keyword=${encodeURIComponent(keyword)}`,
            "Accept":"application/json, text/plain, */*",
            "Accept-Language":"id-ID,id;q=0.9,en;q=0.8",
          },
          cache:"no-store",
          next:{ revalidate:0 }
        })
        if(!resp.ok) throw new Error(`Shopee ${resp.status}`)
        const json:any = await resp.json()
        if(json.error) throw new Error(`Shopee error`)
        const items:any[] = json.items || []
        const out:any[] = []
        for(const el of items){
          const basic = el.item_basic
          if(!basic) continue
          const name = basic.name
          if(!name) continue
          const priceRaw:number = basic.price||0
          const price = priceRaw>1000000 ? Math.round(priceRaw/100000) : Math.round(priceRaw/100)
          const sold:number = basic.historical_sold ?? basic.sold ?? 0
          const rating:number = basic.item_rating?.rating_star ?? 0
          const reviewCount:number = basic.cmt_count ?? 0
          const shopId = basic.shopid
          const itemId = basic.itemid
          const link = `https://shopee.co.id/product-i.${shopId}.${itemId}`
          const imageId:string|undefined = basic.image
          const imageUrl = imageId ? `https://down-id.img.susercontent.com/file/${imageId}` : null
          const ctime:number = basic.ctime || basic.shop_ctime || el.ctime || 0
          const shopAgeDays = ctime>1e9 ? Math.max(0, Math.min(3650, Math.floor((Date.now()/1000 - ctime)/86400))) : 60
          const shopName = basic.shop_name || "Shopee"
          const soldPerDay = sold>0 ? sold/30 : 0
          const [flag, reason] = trustFlag(shopAgeDays, reviewCount, rating)
          const rate = rateForCategory(keyword)
          const comm = commission(price, rate)
          const sc = score(sold, rating, price)
          const isOP = sold>5000 && rating>=4.8
          out.push({ id:`${shopId}-${itemId}`, name, price, sold, rating, reviewCount, shopName, shopAgeDays, imageUrl, link, category:keyword, soldPerDay: Number(soldPerDay.toFixed(1)), commissionRate:rate, commissionEst:comm, score:Number(sc.toFixed(2)), isOP, isFlagged:flag, flagReason:reason })
        }
        const data = { keyword, limit, count: out.length, items: out, fetchedAt: new Date().toISOString() }
        cache.set(key, { at: Date.now(), data })
        return NextResponse.json(data, { headers:{ "Cache-Control":"public, max-age=60" }})
      }catch(e){ lastErr=e; if(attempt===2) throw e }
    }
    throw lastErr
  }catch(e:any){
    return NextResponse.json({ error: e?.message||"fetch failed", keyword }, {status:502})
  }
}
