"use client"
import { useState, useEffect } from "react"
import type { Product } from "@/lib/scoring"

const fmtRp = (n:number)=>"Rp"+n.toLocaleString("id-ID")

export default function Home(){
  const [keyword,setKeyword]=useState("parfum")
  const [minSold,setMinSold]=useState(1000)
  const [minRating,setMinRating]=useState(4.6)
  const [minPrice,setMinPrice]=useState(50000)
  const [maxPrice,setMaxPrice]=useState(300000)
  const [hideFlagged,setHideFlagged]=useState(false)
  const [opOnly,setOpOnly]=useState(false)
  const [affId,setAffId]=useState("")
  const [loading,setLoading]=useState(false)
  const [error,setError]=useState<string|null>(null)
  const [products,setProducts]=useState<Product[]>([])
  const [copied,setCopied]=useState<string|null>(null)

  useEffect(()=>{ const v=localStorage.getItem("aff_id"); if(v) setAffId(v)},[])
  useEffect(()=>{ localStorage.setItem("aff_id", affId)},[affId])

  const toAff = (link:string)=> affId.trim() ? `${link}${link.includes("?")?"&":"?"}aff=${encodeURIComponent(affId.trim())}` : link

  async function doSearch(){
    setLoading(true); setError(null)
    try{
      const r = await fetch(`/api/search?keyword=${encodeURIComponent(keyword)}&limit=30`)
      const j = await r.json()
      if(!r.ok) throw new Error(j.error||"Gagal fetch — Shopee mungkin ngeblok, coba lagi 10 detik")
      let list:Product[] = j.items||[]
      // client filters (mirror Android)
      list = list.filter(p=> p.sold>=minSold && p.rating>=minRating && p.price>=minPrice && p.price<=maxPrice)
      list = list.sort((a,b)=> b.score-a.score)
      setProducts(list)
      if(list.length===0) setError("Tidak ada hasil untuk filter ini — turunkan Min Terjual / Rating")
    }catch(e:any){ setError(e.message||"Gagal") } finally{ setLoading(false) }
  }

  const filtered = products.filter(p=>{
    if(opOnly && !p.isOP) return false
    if(hideFlagged && p.isFlagged) return false
    return true
  })

  function copyAff(p:Product){
    const link = toAff(p.link)
    navigator.clipboard.writeText(link)
    setCopied(p.id); setTimeout(()=>setCopied(null),1500)
  }
  function exportCsv(){
    if(filtered.length===0) return
    const header=["No","Nama","Harga","Terjual","PerHari","Rating","Komisi","Link_aff","Toko","Flag"]
    const rows = filtered.map((p,i)=> [String(i+1),`"${p.name.replace(/"/g,'""')}"`,String(p.price),String(p.sold),String(p.soldPerDay),String(p.rating),String(p.commissionEst), toAff(p.link), p.shopName, p.flagReason||""].join(","))
    const csv=[header.join(","),...rows].join("\n")
    const blob=new Blob([csv],{type:"text/csv;charset=utf-8"})
    const url=URL.createObjectURL(blob)
    const a=document.createElement("a"); a.href=url; a.download=`affiliate_${keyword}_${Date.now()}.csv`; a.click(); URL.revokeObjectURL(url)
  }

  return (
    <div className="min-h-screen">
      {/* Header */}
      <header className="bg-white border-b border-line sticky top-0 z-10">
        <div className="max-w-5xl mx-auto px-4 py-3 flex items-center justify-between">
          <div>
            <div className="font-black text-[18px] leading-none text-ink">Affiliate Hunter</div>
            <div className="text-[10px] tracking-[0.14em] text-muted font-bold">SHOPEE ONLY • FLAT #3368A0</div>
          </div>
          <div className="flex gap-2">
            <a href="https://github.com/Chukie99/affiliate-hunter" target="_blank" className="text-xs bg-warm border border-line px-3 py-1.5 rounded-full font-bold text-muted hover:bg-line">GitHub</a>
            <a href="/api/search?keyword=parfum&limit=5" target="_blank" className="text-xs bg-primary text-white px-3 py-1.5 rounded-full font-bold">API</a>
          </div>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 py-6 space-y-4">
        {/* Onboarding */}
        <div className="bg-[#FFF8E1] border border-[#FDE68A] rounded-2xl p-4">
          <div className="font-bold text-ink text-sm">Cara pakai (orang awam):</div>
          <ol className="text-xs text-muted mt-1 space-y-1 list-decimal ml-4">
            <li>Isi <b>Affiliate ID</b> di bawah (daftar di affiliate.shopee.co.id → dapet aff_xxx) — tanpa ini tidak dapat komisi.</li>
            <li>Ketik kategori bebas (parfum / hijab / skincare / jam) → <b>Cari</b>.</li>
            <li>Filter Terjual & Rating, lalu <b>Copy aff</b> atau <b>Export CSV</b>.</li>
          </ol>
          {!affId && <div className="text-xs text-danger font-bold mt-2">Belum set Aff ID — link belum dapat komisi!</div>}
        </div>

        {/* Aff ID */}
        <div className="bg-white rounded-2xl border border-line p-4 flex flex-col sm:flex-row gap-3 items-start sm:items-end">
          <div className="flex-1 w-full">
            <label className="text-xs font-bold text-muted">Affiliate ID Shopee</label>
            <input value={affId} onChange={e=>setAffId(e.target.value)} placeholder="aff_123abc (kosong = link biasa, tidak komisi)" className="mt-1 w-full border border-line rounded-xl px-3 py-2.5 text-sm outline-none focus:border-primary bg-white" />
            <div className="text-[11px] text-muted mt-1">Link akan jadi <span className="font-mono bg-warm px-1 py-0.5 rounded">?aff=ID</span> — valid kalau ID benar. Untuk komisi real butuh daftar Open API (lihat README).</div>
          </div>
          <div className="text-xs text-muted hidden sm:block">Auto simpan di browser</div>
        </div>

        {/* Search + Filters */}
        <div className="bg-white rounded-2xl border border-line p-4 space-y-3">
          <div className="flex gap-2">
            <input value={keyword} onChange={e=>setKeyword(e.target.value)} onKeyDown={e=> e.key==="Enter" && doSearch()} placeholder="parfum, hijab, skincare, jam ..." className="flex-1 border border-line rounded-xl px-3 py-2.5 text-sm outline-none focus:border-primary" />
            <button onClick={doSearch} disabled={loading} className="bg-primary text-white px-6 py-2.5 rounded-xl font-bold text-sm disabled:opacity-60 hover:bg-[#2a588a]">{loading?"Mencari...":"Cari"}</button>
            <button onClick={exportCsv} disabled={filtered.length===0} className="border border-line bg-white px-4 py-2.5 rounded-xl font-bold text-sm text-ink disabled:opacity-40">Export CSV</button>
          </div>
          {error && <div className="text-xs text-danger bg-red-50 border border-red-200 rounded-xl px-3 py-2">{error}</div>}
          <div className="flex flex-wrap gap-2">
            <label className="inline-flex items-center gap-1.5 text-xs font-bold text-muted border border-line rounded-full px-3 py-1.5 bg-warm cursor-pointer"><input type="checkbox" checked={opOnly} onChange={e=>setOpOnly(e.target.checked)} /> OP only (sold&gt;5k &amp; ★≥4.8)</label>
            <label className="inline-flex items-center gap-1.5 text-xs font-bold text-muted border border-line rounded-full px-3 py-1.5 bg-warm cursor-pointer"><input type="checkbox" checked={hideFlagged} onChange={e=>setHideFlagged(e.target.checked)} /> Hide abal</label>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <div className="text-xs text-muted font-bold">Min terjual: {minSold}</div>
              <input type="range" min={0} max={10000} step={500} value={minSold} onChange={e=>setMinSold(parseInt(e.target.value))} className="w-full accent-primary" />
            </div>
            <div>
              <div className="text-xs text-muted font-bold">Min rating</div>
              <div className="flex gap-1.5 mt-1">{[4.0,4.5,4.6,4.8].map(r=> <button key={r} onClick={()=>setMinRating(r)} className={`px-3 py-1.5 rounded-full text-xs font-bold border ${minRating===r?"bg-primary text-white border-primary":"bg-white text-muted border-line"}`}>{r}</button>)}</div>
            </div>
            <div className="flex gap-2">
              <div className="flex-1"><div className="text-xs text-muted font-bold">Min Rp</div><input type="number" value={minPrice} onChange={e=>setMinPrice(parseInt(e.target.value)||0)} className="w-full border border-line rounded-xl px-3 py-2 text-sm mt-1" /></div>
              <div className="flex-1"><div className="text-xs text-muted font-bold">Max Rp</div><input type="number" value={maxPrice} onChange={e=>setMaxPrice(parseInt(e.target.value)||0)} className="w-full border border-line rounded-xl px-3 py-2 text-sm mt-1" /></div>
            </div>
          </div>
          <div className="text-xs text-muted">Hasil: <b>{filtered.length}</b> / {products.length} {loading&&"• loading..."} — klik Copy aff untuk salin link affiliate.</div>
        </div>

        {/* Results */}
        <div className="grid gap-3">
          {filtered.map(p=> (
            <div key={p.id} className="bg-white rounded-2xl border border-line p-3 flex gap-3">
              <img src={p.imageUrl||""} alt="" className="w-[72px] h-[72px] rounded-xl object-cover bg-warm border border-line flex-shrink-0" onError={e=> (e.currentTarget.style.display="none")} />
              <div className="flex-1 min-w-0 space-y-1">
                <div className="flex flex-wrap gap-1 items-center">
                  {p.isOP && <span className="bg-ok text-white text-[10px] font-black px-2 py-0.5 rounded-full">OP</span>}
                  {p.isFlagged && <span className="bg-danger text-white text-[10px] font-black px-2 py-0.5 rounded-full">FLAG {p.flagReason}</span>}
                  <span className="text-[10px] text-muted ml-auto">score {p.score.toFixed(1)}</span>
                </div>
                <div className="text-[13px] font-semibold text-ink leading-tight line-clamp-2">{p.name}</div>
                <div className="flex flex-wrap gap-x-3 text-xs">
                  <span className="font-black text-primary">{fmtRp(p.price)}</span>
                  <span className="text-muted">Terjual {p.sold} ({p.soldPerDay.toFixed(1)}/hari)</span>
                  <span className="text-warn font-bold">★ {p.rating}</span>
                  <span className="text-muted truncate">{p.shopName} • {p.shopAgeDays} hari</span>
                </div>
                <div className="text-xs text-ok font-bold">Komisi {(p.commissionRate*100).toFixed(0)}% ~ {fmtRp(p.commissionEst)}</div>
                <div className="flex gap-2">
                  <button onClick={()=>copyAff(p)} className={`text-xs font-bold px-3 py-1.5 rounded-full border ${copied===p.id?"bg-ok text-white border-ok":"bg-white text-ink border-line"}`}>{copied===p.id?"Tersalin!":"Copy aff"}</button>
                  <a href={toAff(p.link)} target="_blank" rel="noreferrer" className="text-xs font-bold px-3 py-1.5 rounded-full bg-warm border border-line text-ink">Buka Shopee</a>
                  <a href={`https://wa.me/?text=${encodeURIComponent(toAff(p.link))}`} target="_blank" className="text-xs font-bold px-3 py-1.5 rounded-full bg-[#25D366] text-white">WA</a>
                </div>
              </div>
            </div>
          ))}
          {filtered.length===0 && !loading && products.length>0 && <div className="text-center text-sm text-muted py-8">Tidak ada hasil untuk filter ini.</div>}
          {products.length===0 && !loading && !error && <div className="text-center text-sm text-muted py-8">Belum ada hasil — ketik kategori lalu Cari.</div>}
        </div>

        <div className="text-center text-[11px] text-muted py-4">FLAT #3368A0 • Shopee only (legal, rate-limit, cache 10m) • v2.0.1 • <a className="underline" href="https://github.com/Chukie99/affiliate-hunter">GitHub</a> • Komisi real butuh daftar Shopee Affiliate Open API</div>
      </main>
    </div>
  )
}
