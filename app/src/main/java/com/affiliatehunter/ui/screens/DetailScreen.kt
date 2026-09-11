package com.affiliatehunter.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.affiliatehunter.ui.HomeViewModel
import com.affiliatehunter.ui.theme.*
import com.affiliatehunter.utils.AffiliateLink
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(nav: NavController, id: String, vm: HomeViewModel = hiltViewModel()){
    val s by vm.s.collectAsState()
    var favProduct by remember { mutableStateOf<com.affiliatehunter.data.model.Product?>(null) }
    // Try VM first, else try to reconstruct minimal from favorites via nav arg (already in VM filtered after refresh)
    val p = s.products.find{ it.id==id } ?: s.filtered.find{ it.id==id } ?: favProduct
    // If not found, try load from DB favorites (async)
    LaunchedEffect(id){
        if(p==null){
            // Attempt to find in favorites list via VM favorites set -> we don't have details, skip
        }
    }
    Scaffold(topBar={ TopAppBar(title={Text("Detail")}, navigationIcon={ IconButton(onClick={nav.popBackStack()}){ Icon(Icons.Filled.ArrowBack,null)} }) }, containerColor = BgWarm){ pad ->
        if(p==null){ Box(Modifier.fillMaxSize().padding(pad), contentAlignment=androidx.compose.ui.Alignment.Center){ Column(horizontalAlignment=androidx.compose.ui.Alignment.CenterHorizontally, verticalArrangement=Arrangement.spacedBy(8.dp)){ Text("Produk tidak ditemukan"); Text("ID: $id", fontSize=11.sp, color=Muted); Button(onClick={ nav.popBackStack() }){ Text("Kembali") } } } ; return@Scaffold }
        val ctx = LocalContext.current
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)){
            if(p.imageUrl!=null){
                Card(shape=RoundedCornerShape(14.dp), colors=CardDefaults.cardColors(containerColor=Color.White)){
                    AsyncImage(model=p.imageUrl, contentDescription=null, modifier=Modifier.fillMaxWidth().height(220.dp), contentScale=androidx.compose.ui.layout.ContentScale.Crop)
                }
            }
            Text(p.name, fontWeight=FontWeight.Bold, fontSize=16.sp, color=Ink)
            Card(shape=RoundedCornerShape(14.dp), colors=CardDefaults.cardColors(containerColor=Color.White)){
                Column(Modifier.padding(14.dp), verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Row(horizontalArrangement=Arrangement.SpaceBetween, modifier=Modifier.fillMaxWidth()){
                        Text("Harga Rp"+p.price, fontWeight=FontWeight.Bold, color=Primary)
                        Text("Terjual "+p.sold, color=Muted)
                    }
                    Text("Toko: "+p.shopName+" ("+p.shopAgeDays+" hari)", color=Muted, fontSize=12.sp)
                    Text("Rating "+p.rating+" ("+p.reviewCount+" ulasan)", color=Muted)
                    Text("Per hari: "+"%.1f".format(p.soldPerDay), color=Muted)
                    if(p.velocityBadge!=null) Text("Velocity: "+p.velocityBadge, color=Warn, fontWeight=FontWeight.SemiBold)
                    Text("Komisi real "+(p.commissionRate*100).toInt()+"% ~ Rp"+p.commissionEst, color=Ok, fontWeight=FontWeight.SemiBold)
                    Text("Link: "+p.link, fontSize=11.sp, color=Muted)
                    Text("Link aff: "+AffiliateLink.toAffiliate(p.link, s.affId), fontSize=11.sp, color=Primary)
                    if(p.isFlagged) Text("FLAG: "+(p.flagReason?:"toko berisiko"), color=Red, fontWeight=FontWeight.SemiBold)
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                        Button(onClick={
                            val aff = AffiliateLink.toAffiliate(p.link, s.affId)
                            val clip = ClipData.newPlainText("aff", aff)
                            (ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(clip)
                            Toast.makeText(ctx,if(s.affId.isBlank()) "Set Aff ID dulu biar komisi masuk!" else "Link aff disalin",Toast.LENGTH_SHORT).show()
                        }){ Text("Copy link aff") }
                        OutlinedButton(onClick={
                            val aff = AffiliateLink.toAffiliate(p.link, s.affId)
                            val intent = Intent(Intent.ACTION_SEND).apply{ type="text/plain"; putExtra(Intent.EXTRA_TEXT, aff) }
                            ctx.startActivity(Intent.createChooser(intent,"Share link aff"))
                        }){ Text("Share") }
                    }
                    if(s.affId.isBlank()){
                        Text("Set Affiliate ID di Pengaturan dulu — link tanpa aff tidak dapat komisi.", color=Red, fontSize=11.sp)
                    }
                }
            }
            Card(shape=RoundedCornerShape(14.dp), colors=CardDefaults.cardColors(containerColor=Color.White)){
                Column(Modifier.padding(14.dp)){
                    Text("Velocity 7 hari", fontWeight=FontWeight.SemiBold, color=Ink)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress={ (p.soldPerDay/50.0).coerceIn(0.0,1.0).toFloat() }, modifier=Modifier.fillMaxWidth().height(10.dp), color=Primary)
                    Text("%.1f terjual/hari".format(p.soldPerDay), fontSize=11.sp, color=Muted)
                    Text("Skor hunter: %.2f (log10(terjual) x rating x sweet-spot)".format(p.score), fontSize=11.sp, color=Muted)
                }
            }
        }
    }
}
