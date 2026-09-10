package com.example.macremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(discoveryManager: DiscoveryManager) {
    val connectionState by WebSocketClient.connectionState.collectAsState()
    val errorMessage by WebSocketClient.errorMessage.collectAsState()
    
    var showPinDialog by remember { mutableStateOf(false) }
    var manualAddress by remember { mutableStateOf("") }
    var selectedAddress by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            if (connectionState != ConnectionState.AUTHENTICATED) {
                TopAppBar(
                    title = { Text("Mac Remote") }
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
                            text = "Error: ${errorMessage ?: "Unknown"}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    // Manual URL Entry for Ngrok
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Manual Connection (e.g., Ngrok URL)", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = manualAddress,
                                onValueChange = { manualAddress = it },
                                label = { Text("Address (e.g. wss://...)") },
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
                                Text("Connect")
                            }
                        }
                    }

                    ServerDiscoveryList(discoveryManager) { ip, port ->
                        selectedAddress = "$ip:$port"
                        showPinDialog = true
                    }
                }
                ConnectionState.CONNECTING, ConnectionState.AUTHENTICATING -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                        Text("Connecting...", modifier = Modifier.padding(top = 64.dp))
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

        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text("Enter PIN") },
                text = {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("4-digit PIN") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        WebSocketClient.connect(selectedAddress, pin)
                        showPinDialog = false
                    }) {
                        Text("Connect")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ServerDiscoveryList(discoveryManager: DiscoveryManager, onServerSelected: (String, Int) -> Unit) {
    val servers by discoveryManager.discoveredServers.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Discovered Mac Computers:",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        
        if (servers.isEmpty()) {
            Text(
                text = "Scanning network...",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        LazyColumn {
            items(servers) { server ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onServerSelected(server.ip, server.port) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = server.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "${server.ip}:${server.port}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
