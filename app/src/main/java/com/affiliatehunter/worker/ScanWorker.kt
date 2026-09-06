package com.affiliatehunter.worker
import android.content.Context
import androidx.work.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.affiliatehunter.data.remote.ShopeeRemote
import java.util.concurrent.TimeUnit
class ScanWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params){
    override suspend fun doWork(): Result {
        val favCats = inputData.getStringArray("cats") ?: arrayOf("parfum","hijab")
        var newCount = 0
        for(cat in favCats){
            try{ val res = ShopeeRemote.search(cat, 10); if(res.isNotEmpty()) newCount++ }catch(_:Exception){}
        }
        if(newCount>0) showNotif("$newCount kategori ada update Top 5")
        return Result.success()
    }
    private fun showNotif(text:String){
        val chId="hunter_scan"
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val ch=NotificationChannel(chId,"Hunter Scan",NotificationManager.IMPORTANCE_DEFAULT)
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
        val n=NotificationCompat.Builder(applicationContext,chId).setSmallIcon(android.R.drawable.ic_popup_sync).setContentTitle("Affiliate Hunter").setContentText(text).setAutoCancel(true).build()
        try{ NotificationManagerCompat.from(applicationContext).notify(1001,n) }catch(_:Exception){}
    }
    companion object{
        fun schedule(c: Context, hours: Long=12){
            val req = PeriodicWorkRequestBuilder<ScanWorker>(hours, TimeUnit.HOURS).setInputData(workDataOf("cats" to arrayOf("parfum","hijab"))).build()
            WorkManager.getInstance(c).enqueueUniquePeriodicWork("scan", ExistingPeriodicWorkPolicy.UPDATE, req)
        }
        fun cancel(c: Context){ WorkManager.getInstance(c).cancelUniqueWork("scan") }
    }
}
