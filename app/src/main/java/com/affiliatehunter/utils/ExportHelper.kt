package com.affiliatehunter.utils
import android.content.Context
import android.os.Environment
import com.affiliatehunter.data.model.Product
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter
object ExportHelper {
    fun exportCsv(c: Context, items: List<Product>): File {
        val dir = c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: c.filesDir
        val f = File(dir, "affiliate_terlaris_v2.csv")
        FileWriter(f).use{ fw ->
            val p = CSVPrinter(fw, CSVFormat.DEFAULT.builder().setHeader("No","Nama","Harga","Terjual","PerHari","Rating","Komisi_real","Velocity","Link_aff","Toko","Flag").build())
            items.forEachIndexed{i, it ->
                p.printRecord(i+1,it.name,it.price,it.sold,"%.1f".format(it.soldPerDay),it.rating,it.commissionEst,it.velocityBadge?:"",AffiliateLink.toAffiliate(it.link,""),it.shopName,it.flagReason?:"")
            }
            p.flush()
        }
        return f
    }
}
