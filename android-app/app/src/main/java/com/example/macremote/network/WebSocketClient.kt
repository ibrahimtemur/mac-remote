package com.example.macremote.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, AUTHENTICATING, AUTHENTICATED, ERROR
}

object WebSocketClient {
    private const val TAG = "WebSocketClient"
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    private val gson = Gson()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _screenBitmap = MutableStateFlow<Bitmap?>(null)
    val screenBitmap: StateFlow<Bitmap?> = _screenBitmap

    fun connect(address: String, pin: String) {
        val url = if (address.startsWith("ws://") || address.startsWith("wss://")) {
            address
        } else {
            // Assume it's an IP:Port string, or just an IP
            if (address.contains(":")) "ws://$address" else "ws://$address:8765"
        }
        
        val request = Request.Builder().url(url).build()
        
        _connectionState.value = ConnectionState.CONNECTING
        _errorMessage.value = null

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Connected to server")
                _connectionState.value = ConnectionState.CONNECTED
                authenticate(pin)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    // Simple parsing for auth response
                    if (text.contains("\"type\":\"auth_response\"") || text.contains("\"type\": \"auth_response\"")) {
                        if (text.contains("\"status\":\"success\"") || text.contains("\"status\": \"success\"")) {
                            _connectionState.value = ConnectionState.AUTHENTICATED
                        } else {
                            _connectionState.value = ConnectionState.ERROR
                            _errorMessage.value = "Invalid PIN"
                            webSocket.close(1000, "Invalid PIN")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Parse error", e)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                try {
                    val byteArray = bytes.toByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    if (bitmap != null) {
                        _screenBitmap.value = bitmap
                    }
                    // Immediately ACK to server to request next frame with ZERO queueing lag
                    webSocket.send("{\"type\":\"screen_ack\"}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding image", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Connection failed", t)
                _connectionState.value = ConnectionState.ERROR
                _errorMessage.value = t.message ?: "Connection failed"
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Connection closed")
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        })
    }

    private fun authenticate(pin: String) {
        _connectionState.value = ConnectionState.AUTHENTICATING
        val authMessage = mapOf("type" to "auth", "pin" to pin)
        webSocket?.send(gson.toJson(authMessage))
    }

    fun sendAbsoluteMove(xPercent: Float, yPercent: Float) {
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            val msg = mapOf("type" to "absolute_move", "x" to xPercent, "y" to yPercent)
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun sendAbsoluteDrag(xPercent: Float, yPercent: Float) {
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            val msg = mapOf("type" to "absolute_drag", "x" to xPercent, "y" to yPercent)
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun sendMouseMove(dx: Float, dy: Float) {
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            val msg = mapOf("type" to "mouse_move", "dx" to dx, "dy" to dy)
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun sendMouseClick(button: String = "left", count: Int = 1) {
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            val msg = mapOf("type" to "mouse_click", "button" to button, "count" to count)
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun sendToggleCursor(show: Boolean? = null) {
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            val msg = if (show != null) {
                mapOf("type" to "toggle_cursor", "show" to show)
            } else {
                mapOf("type" to "toggle_cursor")
            }
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun sendScroll(dx: Float, dy: Float) {
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            val msg = mapOf("type" to "scroll", "dx" to dx, "dy" to dy)
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun sendMouseDown(button: String = "left") {
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            val msg = mapOf("type" to "mouse_down", "button" to button)
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun sendMouseUp(button: String = "left") {
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            val msg = mapOf("type" to "mouse_up", "button" to button)
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun sendQuality(level: String) {
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            val msg = mapOf("type" to "set_quality", "level" to level)
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun sendMediaCommand(action: String) {
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            val msg = mapOf("type" to "media", "action" to action)
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun sendSystemCommand(action: String) {
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            val msg = mapOf("type" to "system", "action" to action)
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun sendKeyPress(key: String) {
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            val msg = mapOf("type" to "key_press", "key" to key)
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun sendSpecialKey(key: String) {
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            val msg = mapOf("type" to "special_key", "key" to key)
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
        _screenBitmap.value = null
    }
}
