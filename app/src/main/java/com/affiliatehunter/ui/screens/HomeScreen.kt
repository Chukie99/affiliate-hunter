package com.affiliatehunter.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.affiliatehunter.ui.HomeViewModel
import com.affiliatehunter.utils.AffiliateLink
import com.affiliatehunter.utils.ExportHelper
import com.affiliatehunter.ui.theme.*
import android.content.Intent
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController, vm: HomeViewModel = hiltViewModel()){
    val s by vm.s.collectAsState()
    val ctx = LocalContext.current
    Scaffold(
        topBar={
            TopAppBar(title={Column{
                Text("Affiliate Hunter", fontWeight=FontWeight.Bold, fontSize=18.sp, color=Ink)
                Text("SHOPEE ONLY  •  FLAT #3368A0", fontSize=10.sp, color=Muted)
            }}, colors=TopAppBarDefaults.topAppBarColors(containerColor=Color.White),
            actions={
                IconButton(onClick={nav.navigate("favorites")}){
                    Icon(Icons.Filled.Favorite, contentDescription="fav", tint=Primary)
                }
                IconButton(onClick={nav.navigate("settings")}){
                    Icon(Icons.Filled.Settings, contentDescription="set", tint=Primary)
                }
            })
        },
        containerColor = BgWarm
    ){ pad ->
        LazyColumn(modifier=Modifier.fillMaxSize().padding(pad).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)){
            item{
                // Search bar
                Card(shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color.White), elevation=CardDefaults.cardElevation(2.dp)){
                    Column(Modifier.padding(14.dp), verticalArrangement=Arrangement.spacedBy(10.dp)){
                        OutlinedTextField(value=s.query, onValueChange={vm.setQuery(it)}, label={Text("Kategori bebas")}, placeholder={Text("parfum, hijab, skincare, jam ...")}, singleLine=true, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(12.dp))
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            FilledButton(onClick={vm.search()}, modifier=Modifier.weight(1f)){ if(s.loading) CircularProgressIndicator(modifier=Modifier.size(16.dp), strokeWidth=2.dp, color=Color.White) else Text("Cari") }
                            OutlinedButton(onClick={
                                val f = ExportHelper.exportCsv(ctx, s.filtered)
                                Toast.makeText(ctx,"Export: "+f.name,Toast.LENGTH_SHORT).show()
                                val intent = Intent(Intent.ACTION_SEND).apply{ type="text/csv"; putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(ctx, ctx.packageName+".provider", f)); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                                ctx.startActivity(Intent.createChooser(intent,"Share CSV"))
                            }){ Text("Export") }
                        }
                        if(s.error!=null){ Text(s.error!!, color=Red, fontSize=12.sp) }
                        // Filters
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            FilterChip(selected=s.opOnly, onClick={vm.setOpOnly(!s.opOnly)}, label={Text("OP only")})
                            FilterChip(selected=s.hideFlagged, onClick={vm.setHideFlagged(!s.hideFlagged)}, label={Text("Hide abal")})
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){
                            Column{ Text("Min terjual: "+s.minSold, fontSize=12.sp, color=Muted); Slider(value=s.minSold.toFloat(), onValueChange={vm.setMinSold(it.toInt())}, valueRange=0f..10000f, steps=9) }
                        }
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            Text("Min rating ", fontSize=12.sp, color=Muted, modifier=Modifier.align(Alignment.CenterVertically))
                            listOf(4.0,4.5,4.6,4.8).forEach{ r ->
                                FilterChip(selected=s.minRating==r, onClick={vm.setMinRating(r)}, label={Text("$r")})
                            }
                        }
                        Text("Harga "+s.minPrice+" - "+s.maxPrice, fontSize=12.sp, color=Muted)
                        Text("Hasil: "+s.filtered.size+" / "+s.products.size, fontSize=11.sp, color=Muted)
                    }
                }
            }
            items(s.filtered){ p ->
                Card(shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color.White), modifier=Modifier.fillMaxWidth()){
                    Column(Modifier.padding(12.dp), verticalArrangement=Arrangement.spacedBy(6.dp)){
                        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                            if(p.isOP) Badge("OP", Ok, Color.White)
                            if(p.velocityBadge!=null) Badge(p.velocityBadge, Warn, Color.White)
                            if(p.isFlagged) Badge("FLAG "+(p.flagReason?:""), Red, Color.White)
                            Spacer(Modifier.weight(1f))
                            Text("score %.1f".format(p.score), fontSize=10.sp, color=Muted)
                        }
                        Text(p.name, fontWeight=FontWeight.SemiBold, fontSize=13.sp, color=Ink, maxLines=2, overflow=TextOverflow.Ellipsis)
                        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
                            Text("Rp"+p.price, fontWeight=FontWeight.Bold, fontSize=13.sp, color=Primary)
                            Text("Terjual "+p.sold+"  ("+"%.1f".format(p.soldPerDay)+"/hari)", fontSize=11.sp, color=Muted)
                            Text("★ "+p.rating, fontSize=11.sp, color=Warn)
                        }
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            Text("Komisi "+(p.commissionRate*100).toInt()+"%% ~ Rp"+p.commissionEst, fontSize=11.sp, color=Ok, fontWeight=FontWeight.SemiBold)
                            if(p.isFlagged) Text(p.flagReason?:"", fontSize=10.sp, color=Red)
                        }
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            OutlinedButton(onClick={
                                val aff = AffiliateLink.toAffiliate(p.link, s.affId)
                                val clip = android.content.ClipData.newPlainText("link", aff)
                                (ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(clip)
                                Toast.makeText(ctx,"Link disalin",Toast.LENGTH_SHORT).show()
                            }){ Text("Copy link aff") }
                            Button(onClick={ vm.toggleFav(p) }, colors=ButtonDefaults.buttonColors(containerColor=if(s.favorites.contains(p.id)) Red else Primary)){ Text(if(s.favorites.contains(p.id)) "Hapus fav" else "Favorit") }
                            TextButton(onClick={ nav.navigate("detail/"+p.id) }){ Text("Detail") }
                        }
                    }
                }
            }
        }
    }
}
@Composable private fun Badge(text:String, bg:Color, fg:Color){
    Box(Modifier.background(bg, RoundedCornerShape(8.dp)).padding(horizontal=7.dp, vertical=3.dp)){
        Text(text, color=fg, fontSize=10.sp, fontWeight=FontWeight.Bold)
    }
}
@Composable private fun FilledButton(onClick:()->Unit, modifier:Modifier=Modifier, content:@Composable RowScope.()->Unit){
    Button(onClick=onClick, modifier=modifier, colors=ButtonDefaults.buttonColors(containerColor=Primary)){ content() }
}
