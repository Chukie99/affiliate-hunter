package com.affiliatehunter.ui.screens
import androidx.compose.foundation.layout.*
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
import com.affiliatehunter.ui.SettingsViewModel
import com.affiliatehunter.ui.theme.*
import com.affiliatehunter.worker.ScanWorker
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavController, vm: SettingsViewModel = hiltViewModel()){
    val affId by vm.affId.collectAsState()
    var input by remember(affId){ mutableStateOf(affId) }
    var every12 by remember{ mutableStateOf(true) }
    val ctx = LocalContext.current
    Scaffold(topBar={ TopAppBar(title={Text("Pengaturan")}, navigationIcon={ IconButton(onClick={nav.popBackStack()}){ Icon(Icons.Filled.ArrowBack,null)} }) }, containerColor=BgWarm){ pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)){
            Card(shape=RoundedCornerShape(14.dp), colors=CardDefaults.cardColors(containerColor=Color.White)){
                Column(Modifier.padding(14.dp), verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Text("Affiliate ID Shopee", fontWeight=FontWeight.SemiBold, color=Ink)
                    Text("Tempel ID aff_ kamu biar Copy link langsung jadi link aff", fontSize=11.sp, color=Muted)
                    OutlinedTextField(value=input, onValueChange={input=it}, label={Text("aff_id")}, placeholder={Text("contoh: aff_123abc")}, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(12.dp))
                    Button(onClick={ vm.saveAffId(input) }, colors=ButtonDefaults.buttonColors(containerColor=Primary)){ Text("Simpan") }
                    if(affId.isNotBlank()) Text("Tersimpan: "+affId, fontSize=11.sp, color=Ok)
                }
            }
            Card(shape=RoundedCornerShape(14.dp), colors=CardDefaults.cardColors(containerColor=Color.White)){
                Column(Modifier.padding(14.dp), verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Text("Scheduler OP", fontWeight=FontWeight.SemiBold, color=Ink)
                    Text("Auto scan parfum/hijab tiap 12 jam + notif Top baru", fontSize=11.sp, color=Muted)
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                        FilterChip(selected=every12, onClick={every12=!every12}, label={Text(if(every12) "Tiap 12 jam" else "Tiap 24 jam")})
                        Button(onClick={
                            if(every12) ScanWorker.schedule(ctx, 12) else ScanWorker.schedule(ctx, 24)
                        }, colors=ButtonDefaults.buttonColors(containerColor=Primary)){ Text("Aktifkan") }
                        OutlinedButton(onClick={ ScanWorker.cancel(ctx) }){ Text("Matikan") }
                    }
                }
            }
            Text("v2.0.0-OP  FLAT #3368A0  •  API 24-34  •  Shopee only (legal)", fontSize=10.sp, color=Muted)
        }
    }
}
