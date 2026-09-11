package com.example.macremote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macremote.network.ConnectionState
import com.example.macremote.network.DiscoveryManager
import com.example.macremote.network.WebSocketClient
import com.example.macremote.ui.TrackpadScreen

class MainActivity : ComponentActivity() {
    private lateinit var discoveryManager: DiscoveryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        discoveryManager = DiscoveryManager(this)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(discoveryManager)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        discoveryManager.startDiscovery()
    }

    override fun onPause() {
        super.onPause()
        discoveryManager.stopDiscovery()
    }
}

object AppStrings {
    fun get(key: String, lang: String): String = when (lang) {
        "en" -> when (key) {
            "app_title" -> "Mac Remote"
            "help_tooltip" -> "How to Connect?"
            "manual_title" -> "Manual Connection (e.g., Ngrok URL)"
            "address_label" -> "Address (e.g. wss://...)"
            "connect" -> "Connect"
            "discovered_title" -> "Discovered Mac Computers:"
            "scanning" -> "Scanning network..."
            "connecting" -> "Connecting..."
            "enter_pin" -> "Enter PIN"
            "pin_label" -> "4-digit PIN"
            "cancel" -> "Cancel"
            "error_prefix" -> "Error"
            "guide_title" -> "Mac Remote Setup Guide"
            "guide_step1_title" -> "1. Download Mac App"
            "guide_step1_desc" -> "Download 'MacRemote-macOS.zip' from GitHub Releases to your Mac and unzip it."
            "guide_step2_title" -> "2. Install & Grant Permission"
            "guide_step2_desc" -> "Drag 'Mac Remote.app' to your Applications folder. In System Settings > Privacy & Security > Accessibility, grant permission."
            "guide_step3_title" -> "3. Start Server & Connect"
            "guide_step3_desc" -> "Click 'Start Server' on Mac. Select your Mac from the list below or paste the Ngrok address, then enter the 4-digit PIN."
            "open_releases" -> "Open GitHub Releases"
            "close" -> "Got It"
            else -> key
        }
        else -> when (key) { // "tr"
            "app_title" -> "Mac Remote"
            "help_tooltip" -> "Nasıl Bağlanılır?"
            "manual_title" -> "Manuel Bağlantı (örn. Ngrok URL)"
            "address_label" -> "Adres (örn. wss://...)"
            "connect" -> "Bağlan"
            "discovered_title" -> "Bulunan Mac Bilgisayarlar:"
            "scanning" -> "Ağ taranıyor..."
            "connecting" -> "Bağlanıyor..."
            "enter_pin" -> "PIN Girin"
            "pin_label" -> "4 haneli PIN"
            "cancel" -> "İptal"
            "error_prefix" -> "Hata"
            "guide_title" -> "Mac'e Nasıl Bağlanılır?"
            "guide_step1_title" -> "1. Mac Uygulamasını İndirin"
            "guide_step1_desc" -> "GitHub Releases sayfasından 'MacRemote-macOS.zip' dosyasını Mac'inize indirin ve arşivden çıkarın."
            "guide_step2_title" -> "2. Kurun ve İzin Verin"
            "guide_step2_desc" -> "'Mac Remote.app' uygulamasını Uygulamalar klasörüne taşıyın. Sistem Ayarları > Gizlilik ve Güvenlik > Erişilebilirlik menüsünden Mac Remote'a izin verin."
            "guide_step3_title" -> "3. Sunucuyu Başlatın & Bağlanın"
            "guide_step3_desc" -> "Mac'te 'Start Server' butonuna tıklayın. Ağda otomatik bulunan Mac'i seçin veya Ngrok adresini girip ekrandaki 4 haneli PIN ile bağlanın."
            "open_releases" -> "GitHub Releases Sayfasını Aç"
            "close" -> "Anladım"
            else -> key
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(discoveryManager: DiscoveryManager) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mac_remote_prefs", Context.MODE_PRIVATE) }
    var lang by remember { mutableStateOf(prefs.getString("app_lang", "tr") ?: "tr") }

    val connectionState by WebSocketClient.connectionState.collectAsState()
    val errorMessage by WebSocketClient.errorMessage.collectAsState()
    
    var showGuideDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var manualAddress by remember { mutableStateOf("") }
    var selectedAddress by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            if (connectionState != ConnectionState.AUTHENTICATED) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                AppStrings.get("app_title", lang),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { showGuideDialog = true }) {
                                Icon(
                                    Icons.Default.HelpOutline,
                                    contentDescription = AppStrings.get("help_tooltip", lang),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        FilledTonalButton(
                            onClick = {
                                lang = if (lang == "tr") "en" else "tr"
                                prefs.edit().putString("app_lang", lang).apply()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.padding(end = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (lang == "tr") "🇹🇷 TR" else "🇬🇧 EN",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = if (connectionState == ConnectionState.AUTHENTICATED) {
                Modifier.fillMaxSize()
            } else {
                Modifier.padding(padding)
            }
        ) {
            when (connectionState) {
                ConnectionState.DISCONNECTED, ConnectionState.ERROR -> {
                    if (connectionState == ConnectionState.ERROR) {
                        Text(
                            text = "${AppStrings.get("error_prefix", lang)}: ${errorMessage ?: "Unknown"}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    // Manual URL Entry for Ngrok
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                AppStrings.get("manual_title", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            OutlinedTextField(
                                value = manualAddress,
                                onValueChange = { manualAddress = it },
                                label = { Text(AppStrings.get("address_label", lang)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                            Button(
                                onClick = {
                                    if (manualAddress.isNotBlank()) {
                                        selectedAddress = manualAddress
                                        showPinDialog = true
                                    }
                                },
                                modifier = Modifier.padding(top = 8.dp).align(Alignment.End)
                            ) {
                                Text(AppStrings.get("connect", lang))
                            }
                        }
                    }

                    ServerDiscoveryList(discoveryManager, lang) { ip, port ->
                        selectedAddress = "$ip:$port"
                        showPinDialog = true
                    }
                }
                ConnectionState.CONNECTING, ConnectionState.AUTHENTICATING -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                        Text(AppStrings.get("connecting", lang), modifier = Modifier.padding(top = 64.dp))
                    }
                }
                ConnectionState.CONNECTED -> {
                    // Waiting for auth
                }
                ConnectionState.AUTHENTICATED -> {
                    TrackpadScreen()
                }
            }
        }

        // Help & Setup Guide Dialog
        if (showGuideDialog) {
            AlertDialog(
                onDismissRequest = { showGuideDialog = false },
                title = {
                    Text(
                        AppStrings.get("guide_title", lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GuideStepItem(
                            stepNumber = "1",
                            title = AppStrings.get("guide_step1_title", lang),
                            description = AppStrings.get("guide_step1_desc", lang)
                        )
                        GuideStepItem(
                            stepNumber = "2",
                            title = AppStrings.get("guide_step2_title", lang),
                            description = AppStrings.get("guide_step2_desc", lang)
                        )
                        GuideStepItem(
                            stepNumber = "3",
                            title = AppStrings.get("guide_step3_title", lang),
                            description = AppStrings.get("guide_step3_desc", lang)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ibrahimtemur/mac-remote/releases"))
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.get("open_releases", lang))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGuideDialog = false }) {
                        Text(AppStrings.get("close", lang))
                    }
                }
            )
        }

        // PIN Entry Dialog
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text(AppStrings.get("enter_pin", lang)) },
                text = {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text(AppStrings.get("pin_label", lang)) },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        WebSocketClient.connect(selectedAddress, pin)
                        showPinDialog = false
                    }) {
                        Text(AppStrings.get("connect", lang))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) {
                        Text(AppStrings.get("cancel", lang))
                    }
                }
            )
        }
    }
}

@Composable
fun GuideStepItem(stepNumber: String, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        stepNumber,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ServerDiscoveryList(
    discoveryManager: DiscoveryManager,
    lang: String,
    onServerSelected: (String, Int) -> Unit
) {
    val servers by discoveryManager.discoveredServers.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = AppStrings.get("discovered_title", lang),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        if (servers.isEmpty()) {
            Text(
                text = AppStrings.get("scanning", lang),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        LazyColumn {
            items(servers) { server ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onServerSelected(server.ip, server.port) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = server.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${server.ip}:${server.port}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
