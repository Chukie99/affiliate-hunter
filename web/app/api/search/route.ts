import { NextRequest, NextResponse } from "next/server"
import { trustFlag, rateForCategory, commission, score } from "@/lib/scoring"

type CacheEntry = { at:number; data:any }
const cache = new Map<string, CacheEntry>()
const TTL_MS = 10*60*1000

async function fetchViaShopeeDirect(keyword:string, limit:number){
  const url = `https://shopee.co.id/api/v4/search/search_items?by=relevancy&keyword=${encodeURIComponent(keyword)}&limit=${limit}&newest=0&order=desc&page_type=search&scenario=PAGE_GLOBAL_SEARCH&version=2`
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
  return json
}

async function fetchViaFirecrawl(keyword:string, limit:number){
  const key = process.env.FIRECRAWL_API_KEY
  if(!key) return null
  const apiUrl = `https://shopee.co.id/api/v4/search/search_items?by=relevancy&keyword=${encodeURIComponent(keyword)}&limit=${limit}&newest=0&order=desc&page_type=search&scenario=PAGE_GLOBAL_SEARCH&version=2`
  // Firecrawl scrape the API url directly — bypasses WAF/IP block
  const r = await fetch("https://api.firecrawl.dev/v1/scrape", {
    method:"POST",
    headers:{ "Authorization":`Bearer ${key}`, "Content-Type":"application/json" },
    body: JSON.stringify({
      url: apiUrl,
      formats: ["rawHtml"],
      onlyMainContent: false,
      waitFor: 2000,
    })
  })
  if(!r.ok){
    const t=await r.text()
    throw new Error(`Firecrawl ${r.status} ${t.slice(0,300)}`)
  }
  const j:any = await r.json()
  // Firecrawl returns { data: { rawHtml / html / markdown } } — API returns JSON as rawHtml
  const raw = j.data?.rawHtml || j.data?.html || j.data?.markdown || ""
  // try parse JSON from raw
  try{
    // raw may be JSON string directly
    const parsed = JSON.parse(raw)
    return parsed
  }catch{
    // try extract JSON substring
    const m = raw.match(/\{[\s\S]*\}/)
    if(m) return JSON.parse(m[0])
    throw new Error("Firecrawl parse failed")
  }
}

function toItems(json:any, keyword:string){
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
  return out
}

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

  // 1) try direct
  let lastErr:any=null
  for(let attempt=0; attempt<2; attempt++){
    try{
      if(attempt>0) await new Promise(r=>setTimeout(r, 700))
      const json = await fetchViaShopeeDirect(keyword, limit)
      const out = toItems(json, keyword)
      if(out.length===0) throw new Error("Shopee empty")
      const data = { keyword, limit, count: out.length, items: out, fetchedAt: new Date().toISOString(), source:"shopee-direct" }
      cache.set(key, { at: Date.now(), data })
      return NextResponse.json(data, { headers:{ "Cache-Control":"public, max-age=60" }})
    }catch(e:any){ lastErr=e; }
  }
  // 2) fallback Firecrawl (bypass 403) — real data, NOT fake
  try{
    const fcJson = await fetchViaFirecrawl(keyword, limit)
    if(fcJson){
      const out = toItems(fcJson, keyword)
      if(out.length>0){
        const data = { keyword, limit, count: out.length, items: out, fetchedAt: new Date().toISOString(), source:"firecrawl" }
        cache.set(key, { at: Date.now(), data })
        return NextResponse.json(data, { headers:{ "Cache-Control":"public, max-age=60" }})
      }
    }
  }catch(e:any){ lastErr=e; }
  return NextResponse.json({ error: lastErr?.message||"fetch failed — set FIRECRAWL_API_KEY di Vercel untuk bypass Shopee 403", keyword, hint:"Daftar gratis di firecrawl.dev → copy fc-xxx → Vercel Settings → Environment Variables → FIRECRAWL_API_KEY" }, {status:502})
}
