package com.affiliatehunter.data.remote
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import com.affiliatehunter.data.model.Product
import com.affiliatehunter.utils.Scoring
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
object ShopeeRemote {
    private val client = OkHttpClient()
    suspend fun search(keyword:String, limit:Int=20): List<Product> = withContext(Dispatchers.IO){
        val url = "https://shopee.co.id/api/v4/search/search_items?by=relevancy&keyword=${java.net.URLEncoder.encode(keyword,"UTF-8")}&limit=$limit&newest=0&order=desc&page_type=search&scenario=PAGE_GLOBAL_SEARCH&version=2"
        val req = Request.Builder().url(url).header("User-Agent","Mozilla/5.0").header("Referer","https://shopee.co.id/search?keyword=$keyword").build()
        try{
            val resp = client.newCall(req).execute()
            val body = resp.body?.string() ?: return@withContext emptyList<Product>()
            val json = JsonParser.parseString(body).asJsonObject
            val items = json.getAsJsonArray("items") ?: return@withContext emptyList<Product>()
            val out = mutableListOf<Product>()
            for(el in items){
                val obj = el.asJsonObject
                val basic = obj.getAsJsonObject("item_basic") ?: continue
                val name = basic.get("name")?.asString ?: continue
                val price = (basic.get("price")?.asLong ?: 0L) / 100000
                val sold = basic.get("historical_sold")?.asInt ?: basic.get("sold")?.asInt ?: 0
                val rating = basic.get("item_rating")?.asJsonObject?.get("rating_star")?.asDouble ?: 0.0
                val reviewCount = basic.get("cmt_count")?.asInt ?: 0
                val shopId = basic.get("shopid")?.asLong ?: 0L
                val itemId = basic.get("itemid")?.asLong ?: 0L
                val link = "https://shopee.co.id/product-i.$shopId.$itemId"
                val soldPerDay = if(sold>0) sold/30.0 else 0.0
                val (flag, reason) = Scoring.trustFlag(60, reviewCount, rating)
                val rate = Scoring.rateForCategory(keyword)
                val comm = Scoring.commission(price, rate)
                val score = Scoring.score(sold, rating, price)
                val isOP = sold>5000 && rating>=4.8
                out.add(Product(id="$shopId-$itemId", name=name, price=price, sold=sold, rating=rating, reviewCount=reviewCount, shopName="Shopee", shopAgeDays=60, imageUrl=null, link=link, category=keyword, soldPerDay=soldPerDay, commissionRate=rate, commissionEst=comm, score=score, isOP=isOP, isFlagged=flag, flagReason=reason))
            }
            out
        }catch(e:Exception){ emptyList() }
    }
}
