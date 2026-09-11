/**
 * Shopee Affiliate Open API — generate real offer link if env set, else fallback to ?aff=
 * Docs: https://open.shopee.com/developer-guide/affiliate
 * Env: SHOPEE_AFF_APP_ID, SHOPEE_AFF_SECRET, SHOPEE_AFF_GRAPHQL (optional)
 *
 * Shopee Affiliate uses GraphQL + HMAC-SHA256 signature. If env not set, we fallback gracefully
 * so orang awam tetap bisa pakai (link tidak komisi sampai daftar, tapi tidak error).
 */
import crypto from "crypto"

function sign(payload: string, secret: string){
  return crypto.createHmac("sha256", secret).update(payload).digest("hex")
}

export async function generateAffiliateLink(originalLink: string, affIdFallback?: string): Promise<{ link: string, real: boolean, note?: string }>{
  const appId = process.env.SHOPEE_AFF_APP_ID
  const secret = process.env.SHOPEE_AFF_SECRET
  // If not configured -> fallback to ?aff=
  if(!appId || !secret){
    if(affIdFallback){
      const sep = originalLink.includes("?") ? "&" : "?"
      return { link: `${originalLink}${sep}aff=${encodeURIComponent(affIdFallback)}`, real: false, note: "Set SHOPEE_AFF_APP_ID/SECRET di env untuk link komisi real. Fallback ?aff= dipakai." }
    }
    return { link: originalLink, real: false, note: "Belum ada Affiliate ID — link tidak komisi." }
  }
  // Real Shopee Affiliate API (GraphQL)
  try{
    const endpoint = process.env.SHOPEE_AFF_GRAPHQL || "https://open-api.affiliate.shopee.co.id/graphql"
    const query = `mutation { generateA shortLink(input:{ originUrl:"${originalLink}" }){ shortLink } }`
    // Shopee spec: timestamp + payload + signature header
    const timestamp = Math.floor(Date.now()/1000).toString()
    const payload = `${appId}${timestamp}${query}`
    const signature = sign(payload, secret)
    const resp = await fetch(endpoint, {
      method: "POST",
      headers: {
        "Content-Type":"application/json",
        "Authorization": `SHA256 ${signature}`,
        "X-App-Id": appId,
        "X-Timestamp": timestamp,
      },
      body: JSON.stringify({ query }),
      cache: "no-store"
    })
    if(!resp.ok) throw new Error(`Shopee Aff ${resp.status}`)
    const json:any = await resp.json()
    const shortLink = json?.data?.generateAShortLink?.shortLink || json?.data?.generateShortLink?.shortLink || json?.data?.shortLink
    if(shortLink) return { link: shortLink, real: true }
    // fallback if API returns different shape — still try fallback
    throw new Error("No shortLink in response")
  }catch(e:any){
    if(affIdFallback){
      const sep = originalLink.includes("?") ? "&" : "?"
      return { link: `${originalLink}${sep}aff=${encodeURIComponent(affIdFallback)}`, real: false, note: `Real API gagal (${e.message}) — fallback ?aff=. Cek APP_ID/SECRET & daftar affiliate.shopee.co.id` }
    }
    return { link: originalLink, real: false, note: `Real API gagal: ${e.message}` }
  }
}
