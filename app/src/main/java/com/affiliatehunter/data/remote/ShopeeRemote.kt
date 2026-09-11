package com.affiliatehunter.data.remote
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import com.affiliatehunter.data.model.Product
import com.affiliatehunter.utils.Scoring
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object ShopeeRemote {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Optional backend proxy: if set, fetch via backend to avoid Shopee block on device
    // Set via Settings -> Backend URL (empty = direct to Shopee with retry)
    var backendBaseUrl: String? = "https://web-sigma-tawny-34.vercel.app"

    suspend fun search(keyword: String, limit: Int = 20, affId: String = ""): List<Product> = withContext(Dispatchers.IO) {
        // Try backend first if configured
        if (!backendBaseUrl.isNullOrBlank()) {
            try {
                val bUrl = "${backendBaseUrl!!.trimEnd('/')}/api/search?keyword=${java.net.URLEncoder.encode(keyword, "UTF-8")}&limit=$limit"
                val req = Request.Builder().url(bUrl).header("User-Agent", "Mozilla/5.0").build()
                val resp = client.newCall(req).execute()
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    val json = JsonParser.parseString(body).asJsonObject
                    if (json.has("items")) {
                        return@withContext parseShopeeJson(json.getAsJsonArray("items"), keyword)
                    }
                }
            } catch (_: Exception) { /* fallback to direct */ }
        }
        // Direct Shopee with retry + backoff
        var lastErr: Exception? = null
        for (attempt in 0..2) {
            try {
                if (attempt > 0) delay(800L * attempt)
                return@withContext searchDirect(keyword, limit)
            } catch (e: Exception) { lastErr = e; if (attempt == 2) break }
        }
        // If still failed, return empty (HomeVM will show error)
        emptyList()
    }

    private fun searchDirect(keyword: String, limit: Int): List<Product> {
        val url = "https://shopee.co.id/api/v4/search/search_items?by=relevancy&keyword=${java.net.URLEncoder.encode(keyword, "UTF-8")}&limit=$limit&newest=0&order=desc&page_type=search&scenario=PAGE_GLOBAL_SEARCH&version=2"
        val req = Request.Builder().url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
            .header("Referer", "https://shopee.co.id/search?keyword=${java.net.URLEncoder.encode(keyword, "UTF-8")}")
            .header("Accept", "application/json, text/plain, */*")
            .header("Accept-Language", "id-ID,id;q=0.9,en;q=0.8")
            .header("af-ac-enc-dat", "")
            .build()
        val resp = client.newCall(req).execute()
        if (!resp.isSuccessful) throw RuntimeException("Shopee ${resp.code}: ${resp.message}")
        val body = resp.body?.string() ?: throw RuntimeException("Empty body")
        val json = JsonParser.parseString(body).asJsonObject
        if (json.has("error") && !json.get("error").isJsonNull) throw RuntimeException("Shopee error: ${json.get("error")}")
        val items = json.getAsJsonArray("items") ?: return emptyList()
        return parseShopeeJson(items, keyword)
    }

    private fun parseShopeeJson(items: com.google.gson.JsonArray, keyword: String): List<Product> {
        val out = mutableListOf<Product>()
        for (el in items) {
            val obj = el.asJsonObject
            val basic = obj.getAsJsonObject("item_basic") ?: continue
            val name = basic.get("name")?.asString ?: continue
            val priceRaw = basic.get("price")?.asLong ?: 0L
            val price = if (priceRaw > 1000000) priceRaw / 100000 else priceRaw / 100
            val sold = basic.get("historical_sold")?.asInt ?: basic.get("sold")?.asInt ?: 0
            val rating = basic.get("item_rating")?.asJsonObject?.get("rating_star")?.asDouble ?: 0.0
            val reviewCount = basic.get("cmt_count")?.asInt ?: 0
            val shopId = basic.get("shopid")?.asLong ?: 0L
            val itemId = basic.get("itemid")?.asLong ?: 0L
            val link = "https://shopee.co.id/product-i.$shopId.$itemId"
            // image: Shopee image id -> CDN url
            val imageId = basic.get("image")?.asString
            val imageUrl = if (!imageId.isNullOrBlank()) "https://down-id.img.susercontent.com/file/$imageId" else null
            // shop age: try ctime fields (seconds epoch) -> days
            val shopAgeDays = run {
                val ctime = basic.get("ctime")?.asLong ?: basic.get("shop_ctime")?.asLong ?: obj.get("ctime")?.asLong ?: 0L
                if (ctime > 1000000000L) ((System.currentTimeMillis()/1000 - ctime) / 86400).toInt().coerceIn(0, 3650) else 60
            }
            val shopName = basic.get("shop_name")?.asString ?: "Shopee"
            val soldPerDay = if (sold > 0) sold / 30.0 else 0.0
            val (flag, reason) = Scoring.trustFlag(shopAgeDays, reviewCount, rating)
            val rate = Scoring.rateForCategory(keyword)
            val comm = Scoring.commission(price, rate)
            val score = Scoring.score(sold, rating, price)
            val isOP = sold > 5000 && rating >= 4.8
            out.add(Product(id="$shopId-$itemId", name=name, price=price, sold=sold, rating=rating, reviewCount=reviewCount, shopName=shopName, shopAgeDays=shopAgeDays, imageUrl=imageUrl, link=link, category=keyword, soldPerDay=soldPerDay, commissionRate=rate, commissionEst=comm, score=score, isOP=isOP, isFlagged=flag, flagReason=reason))
        }
        return out
    }
}
