export function score(sold:number, rating:number, price:number){
  const hargaFactor = price>=50000 && price<=300000 ? 1 : 0.7
  return Math.log10(sold+1) * rating * hargaFactor
}
export function commission(price:number, rate:number){ return Math.round(price*rate) }
export function rateForCategory(cat:string){
  const c=cat.toLowerCase()
  if(c.includes("parfum")) return 0.08
  if(c.includes("skincare")||c.includes("skin")) return 0.10
  if(c.includes("hijab")) return 0.05
  if(c.includes("jam")) return 0.07
  if(c.includes("tas")) return 0.08
  if(c.includes("sepatu")) return 0.07
  return 0.08
}
export function trustFlag(shopAgeDays:number, reviewCount:number, rating:number): [boolean, string|null]{
  if(shopAgeDays>=1 && shopAgeDays<=29) return [true, `toko ${shopAgeDays} hari`]
  if(rating>=4.9 && reviewCount<10) return [true, "rating 4.9 tapi ulasan <10"]
  return [false, null]
}
export type Product = {
  id:string; name:string; price:number; sold:number; rating:number; reviewCount:number;
  shopName:string; shopAgeDays:number; imageUrl:string|null; link:string; category:string;
  soldPerDay:number; commissionRate:number; commissionEst:number; score:number; isOP:boolean;
  isFlagged:boolean; flagReason:string|null; velocityBadge?:string|null;
}
