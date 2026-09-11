import { NextRequest, NextResponse } from "next/server"
import { generateAffiliateLink } from "@/lib/affiliate"

export async function POST(req: NextRequest){
  try{
    const { link, affId } = await req.json()
    if(!link || typeof link!=="string") return NextResponse.json({ error:"link required" },{status:400})
    const result = await generateAffiliateLink(link, affId)
    return NextResponse.json(result)
  }catch(e:any){
    return NextResponse.json({ error: e.message||"failed" },{status:500})
  }
}
// GET for easy test: /api/affiliate?link=...&affId=...
export async function GET(req: NextRequest){
  const link = req.nextUrl.searchParams.get("link")||""
  const affId = req.nextUrl.searchParams.get("affId")||undefined
  if(!link) return NextResponse.json({ error:"link query required" },{status:400})
  const result = await generateAffiliateLink(link, affId||undefined)
  return NextResponse.json(result)
}
