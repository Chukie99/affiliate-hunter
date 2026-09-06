package com.affiliatehunter.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.affiliatehunter.ui.FavoritesViewModel
import com.affiliatehunter.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(nav: NavController, vm: FavoritesViewModel = hiltViewModel()){
    val items by vm.items.collectAsState()
    Scaffold(topBar={ TopAppBar(title={Text("Favorit")}, navigationIcon={ IconButton(onClick={nav.popBackStack()}){ Icon(Icons.Filled.ArrowBack,null)} }, actions={ TextButton(onClick={vm.clear()}){ Text("Clear") } }) }, containerColor=BgWarm){ pad ->
        if(items.isEmpty()){
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment=androidx.compose.ui.Alignment.Center){ Text("Belum ada favorit", color=Muted) }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(pad).padding(12.dp), verticalArrangement=Arrangement.spacedBy(8.dp)){
                items(items){ f ->
                    Card(shape=RoundedCornerShape(14.dp), colors=CardDefaults.cardColors(containerColor=Color.White)){
                        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){
                            Column(Modifier.weight(1f)){
                                Text(f.name, fontWeight=FontWeight.SemiBold, fontSize=13.sp, color=Ink, maxLines=2)
                                Text("Rp"+f.price+"  Terjual "+f.sold+"  ★"+f.rating, fontSize=11.sp, color=Muted)
                                Text(f.affiliateLink, fontSize=10.sp, color=Primary, maxLines=1)
                            }
                            IconButton(onClick={vm.remove(f.id)}){ Icon(Icons.Filled.Delete,null, tint=Red) }
                        }
                    }
                }
            }
        }
    }
}
