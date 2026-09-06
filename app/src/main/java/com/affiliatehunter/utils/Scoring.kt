package com.affiliatehunter.utils
import kotlin.math.log10
object Scoring {
    fun score(sold:Int, rating:Double, price:Long, velocityFactor:Double=1.0, trustFactor:Double=1.0): Double {
        val hargaFactor = if(price in 50000..300000) 1.0 else 0.7
        return log10((sold+1).toDouble()) * rating * hargaFactor * velocityFactor * trustFactor
    }
    fun commission(price:Long, rate:Double): Long = (price * rate).toLong()
    fun velocityFlag(soldPerDay: Double, oldPerDay: Double?): String? {
        if(oldPerDay==null || oldPerDay<=0) return null
        val diff = (soldPerDay - oldPerDay)/oldPerDay
        return if(diff >= 0.20) "+${(diff*100).toInt()}% 7d" else null
    }
    fun trustFlag(shopAgeDays:Int, reviewCount:Int, rating:Double): Pair<Boolean,String?> {
        if(shopAgeDays in 1..29) return true to "toko ${shopAgeDays} hari"
        if(rating>=4.9 && reviewCount <10) return true to "rating 4.9 tapi ulasan <10"
        return false to null
    }
    fun rateForCategory(cat:String): Double = when(cat.lowercase()){
        "parfum"->0.08; "skincare"->0.10; "hijab"->0.05; "jam"->0.07; "tas"->0.08; "sepatu"->0.07; else->0.08
    }
}
