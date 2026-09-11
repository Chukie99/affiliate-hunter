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
import coil.compose.AsyncImage
import com.affiliatehunter.ui.HomeViewModel
import com.affiliatehunter.utils.AffiliateLink
import com.affiliatehunter.utils.ExportHelper
import com.affiliatehunter.ui.theme.*
import android.content.Intent
import android.widget.Toast

@Composable
private fun Badge(text: String, bg: Color, fg: Color) {
    Box(Modifier.background(bg, RoundedCornerShape(8.dp)).padding(horizontal=7.dp, vertical=3.dp)) {
        Text(text, color=fg, fontSize=10.sp, fontWeight=FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController, vm: HomeViewModel = hiltViewModel()){
    val s by vm.s.collectAsState()
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { vm.refreshAffId() }
    Scaffold(
        topBar={
            TopAppBar(title={Column{
                Text("Affiliate Hunter", fontWeight=FontWeight.Bold, fontSize=18.sp, color=Ink)
                Text("SHOPEE ONLY  \u2022  FLAT #3368A0", fontSize=10.sp, color=Muted)
            }}, colors=TopAppBarDefaults.topAppBarColors(containerColor=Color.White),
            actions={
                IconButton(onClick={nav.navigate("favorites")}){ Icon(Icons.Filled.Favorite, contentDescription="fav", tint=Primary) }
                IconButton(onClick={nav.navigate("settings")}){ Icon(Icons.Filled.Settings, contentDescription="set", tint=Primary) }
            })
        },
        containerColor = BgWarm
    ){ pad ->
        LazyColumn(modifier=Modifier.fillMaxSize().padding(pad).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)){
            if(s.showOnboarding){
                item{
                    Card(shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF8E1))){
                        Column(Modifier.padding(14.dp), verticalArrangement=Arrangement.spacedBy(8.dp)){
                            Text("Selamat datang!", fontWeight=FontWeight.Bold, color=Ink)
                            Text("1) Atur Affiliate ID di Pengaturan dulu biar link dapat komisi\n2) Ketik kategori (parfum/hijab/skincare) lalu Cari\n3) Filter terjual & rating, lalu Copy link aff atau Export CSV", fontSize=12.sp, color=Muted, lineHeight=16.sp)
                            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                                Button(onClick={ nav.navigate("settings") }, colors=ButtonDefaults.buttonColors(containerColor=Primary)){ Text("Atur Aff ID") }
                                TextButton(onClick={ vm.dismissOnboarding() }){ Text("Mengerti") }
                            }
                        }
                    }
                }
            }
            item{
                Card(shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color.White), elevation=CardDefaults.cardElevation(2.dp)){
                    Column(Modifier.padding(14.dp), verticalArrangement=Arrangement.spacedBy(10.dp)){
                        OutlinedTextField(value=s.query, onValueChange={vm.setQuery(it)}, label={Text("Kategori bebas")}, placeholder={Text("parfum, hijab, skincare, jam ...")}, singleLine=true, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(12.dp))
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            Button(onClick={vm.search()}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=Primary)){ if(s.loading) CircularProgressIndicator(modifier=Modifier.size(16.dp), strokeWidth=2.dp, color=Color.White) else Text("Cari") }
                            OutlinedButton(onClick={
                                if(s.filtered.isEmpty()){ Toast.makeText(ctx,"Belum ada hasil untuk export",Toast.LENGTH_SHORT).show(); return@OutlinedButton }
                                val f = ExportHelper.exportCsv(ctx, s.filtered, s.affId)
                                Toast.makeText(ctx,"Export: "+f.name,Toast.LENGTH_SHORT).show()
                                val intent = Intent(Intent.ACTION_SEND).apply{ type="text/csv"; putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(ctx, ctx.packageName+".provider", f)); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                                ctx.startActivity(Intent.createChooser(intent,"Share CSV"))
                            }){ Text("Export") }
                        }
                        if(s.error!=null){ Text(s.error!!, color=Red, fontSize=12.sp) }
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            FilterChip(selected=s.opOnly, onClick={vm.setOpOnly(!s.opOnly)}, label={Text("OP only")})
                            FilterChip(selected=s.hideFlagged, onClick={vm.setHideFlagged(!s.hideFlagged)}, label={Text("Hide abal")})
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){
                            Column(Modifier.weight(1f)){ Text("Min terjual: "+s.minSold, fontSize=12.sp, color=Muted); Slider(value=s.minSold.toFloat(), onValueChange={vm.setMinSold(it.toInt())}, valueRange=0f..10000f, steps=9) }
                        }
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp), verticalAlignment=Alignment.CenterVertically){
                            Text("Min rating ", fontSize=12.sp, color=Muted)
                            listOf(4.0,4.5,4.6,4.8).forEach{ r ->
                                FilterChip(selected=s.minRating==r, onClick={vm.setMinRating(r)}, label={Text("$r")})
                            }
                        }
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp), verticalAlignment=Alignment.CenterVertically){
                            OutlinedTextField(value=s.minPrice.toString(), onValueChange={ v-> val n=v.filter{ it.isDigit() }.toLongOrNull() ?: 0L; vm.setPrice(n, s.maxPrice) }, label={Text("Min Rp")}, modifier=Modifier.weight(1f), singleLine=true, shape=RoundedCornerShape(10.dp))
                            OutlinedTextField(value=s.maxPrice.toString(), onValueChange={ v-> val n=v.filter{ it.isDigit() }.toLongOrNull() ?: 0L; vm.setPrice(s.minPrice, n) }, label={Text("Max Rp")}, modifier=Modifier.weight(1f), singleLine=true, shape=RoundedCornerShape(10.dp))
                        }
                        Text("Hasil: "+s.filtered.size+" / "+s.products.size, fontSize=11.sp, color=Muted)
                        if(s.filtered.isNotEmpty()){
                            TextButton(onClick={ vm.loadMore() }, modifier=Modifier.align(Alignment.End)){ Text("Load 30 lagi") }
                        }
                    }
                }
            }
            items(s.filtered){ p ->
                Card(shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color.White), modifier=Modifier.fillMaxWidth()){
                    Row(Modifier.padding(12.dp), horizontalArrangement=Arrangement.spacedBy(10.dp)){
                        if(p.imageUrl!=null){
                            AsyncImage(model=p.imageUrl, contentDescription=null, modifier=Modifier.size(72.dp).background(BgWarm, RoundedCornerShape(10.dp)), contentScale=androidx.compose.ui.layout.ContentScale.Crop)
                        } else {
                            Box(Modifier.size(72.dp).background(BgWarm, RoundedCornerShape(10.dp)), contentAlignment=Alignment.Center){ Text("IMG", fontSize=10.sp, color=Muted) }
                        }
                        Column(Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(4.dp)){
                            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                                if(p.isOP) Badge("OP", Ok, Color.White)
                                if(p.velocityBadge!=null) Badge(p.velocityBadge, Warn, Color.White)
                                if(p.isFlagged) Badge("FLAG "+(p.flagReason?:""), Red, Color.White)
                                Spacer(Modifier.weight(1f))
                                Text("score %.1f".format(p.score), fontSize=10.sp, color=Muted)
                            }
                            Text(p.name, fontWeight=FontWeight.SemiBold, fontSize=13.sp, color=Ink, maxLines=2, overflow=TextOverflow.Ellipsis)
                            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                                Text("Rp"+p.price, fontWeight=FontWeight.Bold, fontSize=13.sp, color=Primary)
                                Text("Terjual "+p.sold+"  ("+"%.1f".format(p.soldPerDay)+"/hari)", fontSize=11.sp, color=Muted)
                            }
                            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                                Text("\u2605 "+p.rating, fontSize=11.sp, color=Warn)
                                Text(p.shopName, fontSize=11.sp, color=Muted, maxLines=1, overflow=TextOverflow.Ellipsis, modifier=Modifier.weight(1f))
                            }
                            Text("Komisi "+(p.commissionRate*100).toInt()+"% ~ Rp"+p.commissionEst, fontSize=11.sp, color=Ok, fontWeight=FontWeight.SemiBold)
                            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                                OutlinedButton(onClick={
                                    val aff = AffiliateLink.toAffiliate(p.link, s.affId)
                                    val clip = android.content.ClipData.newPlainText("link", aff)
                                    (ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(clip)
                                    Toast.makeText(ctx, if(s.affId.isBlank()) "Link disalin (set Aff ID dulu biar komisi masuk!)" else "Link aff disalin", Toast.LENGTH_SHORT).show()
                                }){ Text("Copy aff", fontSize=12.sp) }
                                TextButton(onClick={ nav.navigate("detail/${p.id}") }){ Text("Detail", fontSize=12.sp) }
                                IconButton(onClick={ vm.toggleFav(p) }, modifier=Modifier.size(36.dp)){
                                    Icon(if(s.favorites.contains(p.id)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription="fav", tint=if(s.favorites.contains(p.id)) Red else Muted, modifier=Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
            if(s.filtered.isNotEmpty()){
                item{ OutlinedButton(onClick={ vm.loadMore() }, modifier=Modifier.fillMaxWidth()){ Text("Muat 30 lagi") } }
            }
        }
    }
}
