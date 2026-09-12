package com.ibrahimtemur.macremote.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ibrahimtemur.macremote.network.WebSocketClient
import kotlin.math.abs

object TrackpadStrings {
    fun get(key: String, lang: String): String {
        return when (lang) {
            "en" -> when (key) {
                "cursor_on" -> "Cursor Visible"
                "cursor_off" -> "Cursor Hidden"
                "cursor_visibility" -> "Cursor Visibility"
                "quality_tooltip" -> "Quality Setting"
                "q_low" -> "⚡ Low (800p - Fast)"
                "q_medium" -> "⚖️ Medium (1200p - Balanced)"
                "q_high" -> "✨ High (1600p - Crisp Text)"
                "q_ultra" -> "💎 Ultra HD (2200p - Crystal Clear)"
                "fullscreen" -> "Fullscreen"
                "exit_fullscreen" -> "Exit Fullscreen"
                "disconnect" -> "Disconnect"
                "receiving_screen" -> "Receiving screen..."
                "hide_touchpad" -> "Hide Touchpad Panel"
                "show_touchpad" -> "Show Touchpad Panel"
                "toggle_touchpad" -> "Toggle Touchpad"
                "rewind_10s" -> "10s Back"
                "prev" -> "Previous"
                "play_pause" -> "Play / Pause"
                "next" -> "Next"
                "forward_10s" -> "10s Forward"
                "vol_down" -> "Volume Down"
                "vol_up" -> "Volume Up"
                "mute" -> "Mute"
                "toggle_keyboard" -> "Toggle Keyboard"
                "type_text" -> "Type text..."
                "send_text" -> "Send Text"
                "key_space" -> "␣ Space"
                "key_backspace" -> "⌫ Backspace"
                "key_enter" -> "⏎ Enter"
                "key_esc" -> "Esc"
                "trackpad" -> "Trackpad"
                "select_text" -> "Select Text"
                "selection_active" -> "Selection On"
                "close" -> "Close"
                "hint_default" -> "Tap: Click  •  2 Fingers: Scroll & Right Click\nDouble Tap & Drag: Select Text"
                "hint_selecting" -> "📑 Text Selection Active — Drag to select\nTap again to finish"
                "left_click" -> "Left Click"
                "double_click" -> "Double Click"
                "scroll_up" -> "Scroll Up"
                "scroll_down" -> "Scroll Down"
                "right_click" -> "Right Click"
                else -> key
            }
            else -> when (key) { // "tr"
                "cursor_on" -> "İmleç Açık"
                "cursor_off" -> "İmleç Gizli"
                "cursor_visibility" -> "İmleç Görünürlüğü"
                "quality_tooltip" -> "Kalite Ayarı"
                "q_low" -> "⚡ Düşük (800p - Hızlı)"
                "q_medium" -> "⚖️ Orta (1200p - Dengeli)"
                "q_high" -> "✨ Yüksek (1600p - Net Metin)"
                "q_ultra" -> "💎 Ultra HD (2200p - Kristal Net)"
                "fullscreen" -> "Tam Ekran"
                "exit_fullscreen" -> "Tam Ekrandan Çık"
                "disconnect" -> "Bağlantıyı Kes"
                "receiving_screen" -> "Ekran alınıyor..."
                "hide_touchpad" -> "Touchpad Panelini Gizle"
                "show_touchpad" -> "Touchpad Panelini Göster"
                "toggle_touchpad" -> "Touchpad Aç/Kapat"
                "rewind_10s" -> "10 sn Geri"
                "prev" -> "Önceki"
                "play_pause" -> "Oynat / Durdur"
                "next" -> "Sonraki"
                "forward_10s" -> "10 sn İleri"
                "vol_down" -> "Ses Kıs"
                "vol_up" -> "Ses Aç"
                "mute" -> "Sessiz"
                "toggle_keyboard" -> "Klavye Aç / Kapat"
                "type_text" -> "Metin yazın..."
                "send_text" -> "Metni Gönder"
                "key_space" -> "␣ Boşluk"
                "key_backspace" -> "⌫ Sil"
                "key_enter" -> "⏎ Enter"
                "key_esc" -> "Esc"
                "trackpad" -> "Trackpad"
                "select_text" -> "Metin Seç"
                "selection_active" -> "Seçim Açık"
                "close" -> "Kapat"
                "hint_default" -> "Dokun: Tıkla  •  2 Parmak: Scroll & Sağ Tık\nÇift Dokunup Kaydır: Metin Seç"
                "hint_selecting" -> "📑 Metin Seçimi Aktif — Kaydırarak seçin\nBitirmek için tekrar dokunun"
                "left_click" -> "Sol Tık"
                "double_click" -> "Çift Tık"
                "scroll_up" -> "Yukarı Kaydır"
                "scroll_down" -> "Aşağı Kaydır"
                "right_click" -> "Sağ Tık"
                else -> key
            }
        }
    }
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun TrackpadScreen(modifier: Modifier = Modifier) {
    val screenBitmap by WebSocketClient.screenBitmap.collectAsState()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mac_remote_prefs", Context.MODE_PRIVATE) }
    var lang by remember { mutableStateOf(prefs.getString("app_lang", "tr") ?: "tr") }
    val activity = context as? Activity
    val haptic = LocalHapticFeedback.current

    var isFullscreen by remember { mutableStateOf(false) }
    var isTouchpadOpen by remember { mutableStateOf(true) }
    var showPreviewCursor by remember { mutableStateOf(true) }
    var isKeyboardOpen by remember { mutableStateOf(false) }
    var keyboardText by remember { mutableStateOf("") }
    var qualityLevel by remember { mutableStateOf("high") }
    var showQualityMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isKeyboardOpen) {
        if (isKeyboardOpen) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
        }
    }

    // Toggle fullscreen side effect (Immersive mode without rigid orientation locks)
    LaunchedEffect(isFullscreen) {
        if (activity != null) {
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)

            if (isFullscreen) {
                // Let sensor or device decide orientation freely, avoiding hard locks flagged on large screens / Android 16+
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                // Restore orientation to follow user/sensor preference without locking
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    if (isFullscreen) {
        // Landscape Fullscreen View
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Fullscreen Canvas with tap-to-click & absolute navigation
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(screenBitmap) {
                        detectTapGestures(
                            onTap = { offset ->
                                val bmp = screenBitmap ?: return@detectTapGestures
                                val scale = minOf(size.width.toFloat() / bmp.width, size.height.toFloat() / bmp.height)
                                val scaledW = bmp.width * scale
                                val scaledH = bmp.height * scale
                                val left = (size.width - scaledW) / 2f
                                val top = (size.height - scaledH) / 2f

                                val imgX = offset.x - left
                                val imgY = offset.y - top

                                if (imgX in 0f..scaledW && imgY in 0f..scaledH) {
                                    val pctX = (imgX / scaledW).coerceIn(0f, 1f)
                                    val pctY = (imgY / scaledH).coerceIn(0f, 1f)
                                    WebSocketClient.sendAbsoluteMove(pctX, pctY)
                                    WebSocketClient.sendMouseClick("left", 1)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            onDoubleTap = { offset ->
                                val bmp = screenBitmap ?: return@detectTapGestures
                                val scale = minOf(size.width.toFloat() / bmp.width, size.height.toFloat() / bmp.height)
                                val scaledW = bmp.width * scale
                                val scaledH = bmp.height * scale
                                val left = (size.width - scaledW) / 2f
                                val top = (size.height - scaledH) / 2f

                                val imgX = offset.x - left
                                val imgY = offset.y - top

                                if (imgX in 0f..scaledW && imgY in 0f..scaledH) {
                                    val pctX = (imgX / scaledW).coerceIn(0f, 1f)
                                    val pctY = (imgY / scaledH).coerceIn(0f, 1f)
                                    WebSocketClient.sendAbsoluteMove(pctX, pctY)
                                    WebSocketClient.sendMouseClick("left", 2)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            onLongPress = { offset ->
                                val bmp = screenBitmap ?: return@detectTapGestures
                                val scale = minOf(size.width.toFloat() / bmp.width, size.height.toFloat() / bmp.height)
                                val scaledW = bmp.width * scale
                                val scaledH = bmp.height * scale
                                val left = (size.width - scaledW) / 2f
                                val top = (size.height - scaledH) / 2f

                                val imgX = offset.x - left
                                val imgY = offset.y - top

                                if (imgX in 0f..scaledW && imgY in 0f..scaledH) {
                                    val pctX = (imgX / scaledW).coerceIn(0f, 1f)
                                    val pctY = (imgY / scaledH).coerceIn(0f, 1f)
                                    WebSocketClient.sendAbsoluteMove(pctX, pctY)
                                    WebSocketClient.sendMouseClick("right", 1)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        )
                    }
            ) {
                if (screenBitmap != null) {
                    val bmp = screenBitmap!!
                    val scale = minOf(size.width.toFloat() / bmp.width, size.height.toFloat() / bmp.height)
                    val scaledW = bmp.width * scale
                    val scaledH = bmp.height * scale
                    val left = (size.width - scaledW) / 2f
                    val top = (size.height - scaledH) / 2f

                    drawImage(
                        image = bmp.asImageBitmap(),
                        dstOffset = IntOffset(left.toInt(), top.toInt()),
                        dstSize = IntSize(scaledW.toInt(), scaledH.toInt())
                    )
                }
            }

            // Floating Controls in Landscape
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language Toggle in Landscape
                FilledTonalButton(
                    onClick = {
                        lang = if (lang == "tr") "en" else "tr"
                        prefs.edit().putString("app_lang", lang).apply()
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                    )
                ) {
                    Text(
                        if (lang == "tr") "🇹🇷 TR" else "🇬🇧 EN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quality selector in landscape
                Box {
                    FilledTonalIconButton(
                        onClick = { showQualityMenu = true },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                        )
                    ) {
                        Icon(Icons.Default.HighQuality, contentDescription = TrackpadStrings.get("quality_tooltip", lang))
                    }
                    DropdownMenu(
                        expanded = showQualityMenu,
                        onDismissRequest = { showQualityMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(TrackpadStrings.get("q_low", lang)) },
                            onClick = {
                                qualityLevel = "low"
                                WebSocketClient.sendQuality("low")
                                showQualityMenu = false
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(TrackpadStrings.get("q_medium", lang)) },
                            onClick = {
                                qualityLevel = "medium"
                                WebSocketClient.sendQuality("medium")
                                showQualityMenu = false
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(TrackpadStrings.get("q_high", lang)) },
                            onClick = {
                                qualityLevel = "high"
                                WebSocketClient.sendQuality("high")
                                showQualityMenu = false
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(TrackpadStrings.get("q_ultra", lang)) },
                            onClick = {
                                qualityLevel = "ultra"
                                WebSocketClient.sendQuality("ultra")
                                showQualityMenu = false
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    }
                }

                // Cursor overlay toggle
                FilledTonalIconButton(
                    onClick = {
                        showPreviewCursor = !showPreviewCursor
                        WebSocketClient.sendToggleCursor(showPreviewCursor)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                    )
                ) {
                    Icon(
                        if (showPreviewCursor) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = TrackpadStrings.get("cursor_visibility", lang)
                    )
                }

                FilledTonalIconButton(
                    onClick = { isTouchpadOpen = !isTouchpadOpen },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                    )
                ) {
                    Icon(Icons.Default.TouchApp, contentDescription = TrackpadStrings.get("toggle_touchpad", lang))
                }

                FilledTonalIconButton(
                    onClick = { isFullscreen = false },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = TrackpadStrings.get("exit_fullscreen", lang))
                }
            }

            // Floating bottom touchpad panel in landscape
            AnimatedVisibility(
                visible = isTouchpadOpen,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                TouchpadPanel(
                    modifier = Modifier
                        .width(340.dp)
                        .height(190.dp),
                    lang = lang,
                    onClose = { isTouchpadOpen = false }
                )
            }
        }
    } else {
        // Portrait Mode
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
        ) {
            // Top Bar with Status Bar Inset Protection
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DesktopMac,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Mac Remote",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Language Toggle Button (Right of Mac Remote title, next to eye icon)
                        FilledTonalButton(
                            onClick = {
                                lang = if (lang == "tr") "en" else "tr"
                                prefs.edit().putString("app_lang", lang).apply()
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                if (lang == "tr") "🇹🇷 TR" else "🇬🇧 EN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Cursor toggle button
                        FilledTonalIconButton(
                            onClick = {
                                showPreviewCursor = !showPreviewCursor
                                WebSocketClient.sendToggleCursor(showPreviewCursor)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                if (showPreviewCursor) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showPreviewCursor) TrackpadStrings.get("cursor_on", lang) else TrackpadStrings.get("cursor_off", lang),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Quality Selector Button
                        Box {
                            FilledTonalButton(
                                onClick = { showQualityMenu = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.HighQuality,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    when (qualityLevel) {
                                        "low" -> "800p"
                                        "medium" -> "1200p"
                                        "high" -> "1600p"
                                        "ultra" -> "2200p"
                                        else -> "1600p"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            DropdownMenu(
                                expanded = showQualityMenu,
                                onDismissRequest = { showQualityMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(TrackpadStrings.get("q_low", lang)) },
                                    onClick = {
                                        qualityLevel = "low"
                                        WebSocketClient.sendQuality("low")
                                        showQualityMenu = false
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(TrackpadStrings.get("q_medium", lang)) },
                                    onClick = {
                                        qualityLevel = "medium"
                                        WebSocketClient.sendQuality("medium")
                                        showQualityMenu = false
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(TrackpadStrings.get("q_high", lang)) },
                                    onClick = {
                                        qualityLevel = "high"
                                        WebSocketClient.sendQuality("high")
                                        showQualityMenu = false
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(TrackpadStrings.get("q_ultra", lang)) },
                                    onClick = {
                                        qualityLevel = "ultra"
                                        WebSocketClient.sendQuality("ultra")
                                        showQualityMenu = false
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                )
                            }
                        }

                        // Fullscreen Button
                        FilledTonalIconButton(
                            onClick = { isFullscreen = true },
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Fullscreen,
                                contentDescription = TrackpadStrings.get("fullscreen", lang),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Disconnect Button
                        FilledTonalIconButton(
                            onClick = { WebSocketClient.disconnect() },
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f),
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(
                                Icons.Default.PowerSettingsNew,
                                contentDescription = TrackpadStrings.get("disconnect", lang),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Screen View Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(screenBitmap) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val bmp = screenBitmap ?: return@detectTapGestures
                                    val scale = minOf(size.width.toFloat() / bmp.width, size.height.toFloat() / bmp.height)
                                    val scaledW = bmp.width * scale
                                    val scaledH = bmp.height * scale
                                    val left = (size.width - scaledW) / 2f
                                    val top = (size.height - scaledH) / 2f

                                    val imgX = offset.x - left
                                    val imgY = offset.y - top

                                    if (imgX in 0f..scaledW && imgY in 0f..scaledH) {
                                        val pctX = (imgX / scaledW).coerceIn(0f, 1f)
                                        val pctY = (imgY / scaledH).coerceIn(0f, 1f)
                                        WebSocketClient.sendAbsoluteMove(pctX, pctY)
                                        WebSocketClient.sendMouseClick("left", 1)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                },
                                onDoubleTap = { offset ->
                                    val bmp = screenBitmap ?: return@detectTapGestures
                                    val scale = minOf(size.width.toFloat() / bmp.width, size.height.toFloat() / bmp.height)
                                    val scaledW = bmp.width * scale
                                    val scaledH = bmp.height * scale
                                    val left = (size.width - scaledW) / 2f
                                    val top = (size.height - scaledH) / 2f

                                    val imgX = offset.x - left
                                    val imgY = offset.y - top

                                    if (imgX in 0f..scaledW && imgY in 0f..scaledH) {
                                        val pctX = (imgX / scaledW).coerceIn(0f, 1f)
                                        val pctY = (imgY / scaledH).coerceIn(0f, 1f)
                                        WebSocketClient.sendAbsoluteMove(pctX, pctY)
                                        WebSocketClient.sendMouseClick("left", 2)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                },
                                onLongPress = { offset ->
                                    val bmp = screenBitmap ?: return@detectTapGestures
                                    val scale = minOf(size.width.toFloat() / bmp.width, size.height.toFloat() / bmp.height)
                                    val scaledW = bmp.width * scale
                                    val scaledH = bmp.height * scale
                                    val left = (size.width - scaledW) / 2f
                                    val top = (size.height - scaledH) / 2f

                                    val imgX = offset.x - left
                                    val imgY = offset.y - top

                                    if (imgX in 0f..scaledW && imgY in 0f..scaledH) {
                                        val pctX = (imgX / scaledW).coerceIn(0f, 1f)
                                        val pctY = (imgY / scaledH).coerceIn(0f, 1f)
                                        WebSocketClient.sendAbsoluteMove(pctX, pctY)
                                        WebSocketClient.sendMouseClick("right", 1)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            )
                        }
                ) {
                    if (screenBitmap != null) {
                        val bmp = screenBitmap!!
                        val scale = minOf(size.width.toFloat() / bmp.width, size.height.toFloat() / bmp.height)
                        val scaledW = bmp.width * scale
                        val scaledH = bmp.height * scale
                        val left = (size.width - scaledW) / 2f
                        val top = (size.height - scaledH) / 2f

                        drawImage(
                            image = bmp.asImageBitmap(),
                            dstOffset = IntOffset(left.toInt(), top.toInt()),
                            dstSize = IntSize(scaledW.toInt(), scaledH.toInt())
                        )
                    }
                }

                if (screenBitmap == null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(TrackpadStrings.get("receiving_screen", lang), color = Color.White)
                    }
                }
            }

            // Touchpad Toggle Button
            FilledTonalButton(
                onClick = { isTouchpadOpen = !isTouchpadOpen },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isTouchpadOpen) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    if (isTouchpadOpen) Icons.Default.KeyboardArrowDown else Icons.Default.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (isTouchpadOpen) TrackpadStrings.get("hide_touchpad", lang) else TrackpadStrings.get("show_touchpad", lang),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // Collapsible Bottom Touchpad Panel
            AnimatedVisibility(
                visible = isTouchpadOpen,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                TouchpadPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    lang = lang,
                    onClose = { isTouchpadOpen = false }
                )
            }

            // Rich Media & System Controls Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    // Row 1: Playback & Video Seeking (Rewind 10s, Prev, Play/Pause, Next, Forward 10s)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                WebSocketClient.sendMediaCommand("rewind")
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = TrackpadStrings.get("rewind_10s", lang))
                        }

                        IconButton(
                            onClick = {
                                WebSocketClient.sendMediaCommand("prev")
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = TrackpadStrings.get("prev", lang))
                        }

                        FilledIconButton(
                            onClick = {
                                WebSocketClient.sendMediaCommand("play_pause")
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = TrackpadStrings.get("play_pause", lang))
                        }

                        IconButton(
                            onClick = {
                                WebSocketClient.sendMediaCommand("next")
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = TrackpadStrings.get("next", lang))
                        }

                        IconButton(
                            onClick = {
                                WebSocketClient.sendMediaCommand("forward")
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = TrackpadStrings.get("forward_10s", lang))
                        }
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    // Row 2: Volume, Mute & Useful Shortcuts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                WebSocketClient.sendMediaCommand("vol_down")
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        ) {
                            Icon(Icons.Default.VolumeDown, contentDescription = TrackpadStrings.get("vol_down", lang))
                        }

                        IconButton(
                            onClick = {
                                WebSocketClient.sendMediaCommand("vol_up")
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = TrackpadStrings.get("vol_up", lang))
                        }

                        IconButton(
                            onClick = {
                                WebSocketClient.sendMediaCommand("mute")
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        ) {
                            Icon(Icons.Default.VolumeOff, contentDescription = TrackpadStrings.get("mute", lang))
                        }

                        FilledTonalIconButton(
                            onClick = {
                                isKeyboardOpen = !isKeyboardOpen
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (isKeyboardOpen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(
                                if (isKeyboardOpen) Icons.Default.KeyboardHide else Icons.Default.Keyboard,
                                contentDescription = TrackpadStrings.get("toggle_keyboard", lang)
                            )
                        }
                    }

                    // Collapsible Keyboard Input Area
                    AnimatedVisibility(
                        visible = isKeyboardOpen,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = keyboardText,
                                    onValueChange = { keyboardText = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequester),
                                    placeholder = { Text(TrackpadStrings.get("type_text", lang), style = MaterialTheme.typography.bodySmall) },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            if (keyboardText.isNotEmpty()) {
                                                val textToSend = keyboardText
                                                keyboardText = ""
                                                for (char in textToSend) {
                                                    WebSocketClient.sendKeyPress(char.toString())
                                                }
                                            }
                                            WebSocketClient.sendSpecialKey("enter")
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                )

                                FilledIconButton(
                                    onClick = {
                                        if (keyboardText.isNotEmpty()) {
                                            val textToSend = keyboardText
                                            keyboardText = ""
                                            for (char in textToSend) {
                                                WebSocketClient.sendKeyPress(char.toString())
                                            }
                                        }
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = TrackpadStrings.get("send_text", lang))
                                }
                            }

                            // Quick helper keys: Boşluk, Sil, Enter, Esc
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        WebSocketClient.sendSpecialKey("space")
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(TrackpadStrings.get("key_space", lang), style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                                }

                                FilledTonalButton(
                                    onClick = {
                                        WebSocketClient.sendSpecialKey("backspace")
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(TrackpadStrings.get("key_backspace", lang), style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                                }

                                FilledTonalButton(
                                    onClick = {
                                        WebSocketClient.sendSpecialKey("enter")
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(TrackpadStrings.get("key_enter", lang), style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                                }

                                FilledTonalButton(
                                    onClick = {
                                        WebSocketClient.sendSpecialKey("esc")
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(TrackpadStrings.get("key_esc", lang), style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * High-performance Mac-style Touchpad Surface Panel
 * Supports:
 * - 1-finger drag: move mouse cursor
 * - 1-finger tap (< 250ms): Left Click
 * - 1-finger long-press (> 400ms): Right Click
 * - 2-finger drag: vertical/horizontal scroll
 * - Physical Sol Tık, Çift Tık, Sağ Tık buttons
 */
@Composable
fun TouchpadPanel(
    modifier: Modifier = Modifier,
    lang: String = "tr",
    onClose: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    var isSelecting by remember { mutableStateOf(false) }
    var lastTapTime by remember { mutableStateOf(0L) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        TrackpadStrings.get("trackpad", lang),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Dedicated Text Selection Mode button
                    FilledTonalButton(
                        onClick = {
                            isSelecting = !isSelecting
                            if (isSelecting) {
                                WebSocketClient.sendMouseDown("left")
                            } else {
                                WebSocketClient.sendMouseUp("left")
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSelecting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isSelecting) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            if (isSelecting) Icons.Default.Check else Icons.Default.SelectAll,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isSelecting) TrackpadStrings.get("selection_active", lang) else TrackpadStrings.get("select_text", lang),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    if (onClose != null) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = TrackpadStrings.get("close", lang), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Unified Smooth Touch Surface (Eliminating gesture conflict & Supporting Text Drag-Selection)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val startTime = System.currentTimeMillis()
                            var totalMovedDistance = 0f
                            var lastPos = down.position
                            var isRightClickTriggered = false

                            // Double-tap to drag / select text
                            val isDoubleTapDrag = (startTime - lastTapTime < 320)
                            var isDragActive = isDoubleTapDrag
                            if (isDoubleTapDrag) {
                                WebSocketClient.sendMouseDown("left")
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }

                            while (true) {
                                val event = awaitPointerEvent()
                                val activePointers = event.changes.filter { it.pressed }

                                if (activePointers.isEmpty()) {
                                    // All pointers lifted
                                    val duration = System.currentTimeMillis() - startTime
                                    if (isDragActive) {
                                        WebSocketClient.sendMouseUp("left")
                                        isDragActive = false
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } else if (totalMovedDistance < 18f && !isRightClickTriggered) {
                                        if (duration < 280) {
                                            // Quick Tap -> Left Click (or end selection)
                                            if (isSelecting) {
                                                isSelecting = false
                                                WebSocketClient.sendMouseUp("left")
                                            } else {
                                                WebSocketClient.sendMouseClick("left", 1)
                                            }
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            lastTapTime = System.currentTimeMillis()
                                        }
                                    }
                                    break
                                }

                                if (activePointers.size == 1) {
                                    val change = activePointers.first()
                                    val delta = change.position - lastPos
                                    val dist = delta.getDistance()

                                    if (dist > 1.2f) {
                                        totalMovedDistance += dist
                                        // Speed acceleration multiplier
                                        val multiplier = if (dist > 10f) 2.2f else 1.6f
                                        WebSocketClient.sendMouseMove(delta.x * multiplier, delta.y * multiplier)
                                        lastPos = change.position
                                        change.consume()
                                    }

                                    // Long press without moving -> Right Click (when not selecting)
                                    val duration = System.currentTimeMillis() - startTime
                                    if (!isDragActive && !isSelecting && duration > 420 && totalMovedDistance < 15f && !isRightClickTriggered) {
                                        isRightClickTriggered = true
                                        WebSocketClient.sendMouseClick("right", 1)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                } else if (activePointers.size == 2) {
                                    // 2-Finger Scroll
                                    val p0 = activePointers[0]
                                    val deltaY = (p0.position.y - lastPos.y) / 4f
                                    val deltaX = (p0.position.x - lastPos.x) / 4f
                                    if (abs(deltaY) > 0.4f || abs(deltaX) > 0.4f) {
                                        WebSocketClient.sendScroll(deltaX, -deltaY)
                                        totalMovedDistance += 20f
                                    }
                                    lastPos = p0.position
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isSelecting) TrackpadStrings.get("hint_selecting", lang)
                    else TrackpadStrings.get("hint_default", lang),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    color = if (isSelecting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dedicated Physical Click & Scroll Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Click
                Button(
                    onClick = {
                        WebSocketClient.sendMouseClick("left", 1)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        TrackpadStrings.get("left_click", lang),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Double Click
                FilledTonalButton(
                    onClick = {
                        WebSocketClient.sendMouseClick("left", 2)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        TrackpadStrings.get("double_click", lang),
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Scroll Up
                FilledTonalIconButton(
                    onClick = {
                        WebSocketClient.sendScroll(0f, 4f)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = TrackpadStrings.get("scroll_up", lang), modifier = Modifier.size(20.dp))
                }

                // Scroll Down
                FilledTonalIconButton(
                    onClick = {
                        WebSocketClient.sendScroll(0f, -4f)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = TrackpadStrings.get("scroll_down", lang), modifier = Modifier.size(20.dp))
                }

                // Right Click
                Button(
                    onClick = {
                        WebSocketClient.sendMouseClick("right", 1)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        TrackpadStrings.get("right_click", lang),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
