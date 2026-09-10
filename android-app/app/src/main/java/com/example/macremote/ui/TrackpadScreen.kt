package com.example.macremote.ui

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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.macremote.network.WebSocketClient
import kotlin.math.abs

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun TrackpadScreen(modifier: Modifier = Modifier) {
    val screenBitmap by WebSocketClient.screenBitmap.collectAsState()
    val context = LocalContext.current
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

    // Toggle fullscreen side effect
    LaunchedEffect(isFullscreen) {
        if (activity != null) {
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)

            if (isFullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
                // Quality selector in landscape
                Box {
                    FilledTonalIconButton(
                        onClick = { showQualityMenu = true },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                        )
                    ) {
                        Icon(Icons.Default.HighQuality, contentDescription = "Kalite Ayarı")
                    }
                    DropdownMenu(
                        expanded = showQualityMenu,
                        onDismissRequest = { showQualityMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("⚡ Düşük (800p - Hızlı)") },
                            onClick = {
                                qualityLevel = "low"
                                WebSocketClient.sendQuality("low")
                                showQualityMenu = false
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("⚖️ Orta (1200p - Dengeli)") },
                            onClick = {
                                qualityLevel = "medium"
                                WebSocketClient.sendQuality("medium")
                                showQualityMenu = false
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("✨ Yüksek (1600p - Net Metin)") },
                            onClick = {
                                qualityLevel = "high"
                                WebSocketClient.sendQuality("high")
                                showQualityMenu = false
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("💎 Ultra HD (2200p - Kristal Net)") },
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
                        contentDescription = "İmleç Görünürlüğü"
                    )
                }

                FilledTonalIconButton(
                    onClick = { isTouchpadOpen = !isTouchpadOpen },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                    )
                ) {
                    Icon(Icons.Default.TouchApp, contentDescription = "Touchpad Aç/Kapat")
                }

                FilledTonalIconButton(
                    onClick = { isFullscreen = false },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tam Ekrandan Çık")
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
        ) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
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
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                contentDescription = if (showPreviewCursor) "İmleç Açık" else "İmleç Gizli",
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
                                    text = { Text("⚡ Düşük (800p - Hızlı)") },
                                    onClick = {
                                        qualityLevel = "low"
                                        WebSocketClient.sendQuality("low")
                                        showQualityMenu = false
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("⚖️ Orta (1200p - Dengeli)") },
                                    onClick = {
                                        qualityLevel = "medium"
                                        WebSocketClient.sendQuality("medium")
                                        showQualityMenu = false
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("✨ Yüksek (1600p - Net Metin)") },
                                    onClick = {
                                        qualityLevel = "high"
                                        WebSocketClient.sendQuality("high")
                                        showQualityMenu = false
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("💎 Ultra HD (2200p - Kristal Net)") },
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
                                contentDescription = "Tam Ekran",
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
                                contentDescription = "Bağlantıyı Kes",
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
                        Text("Ekran alınıyor...", color = Color.White)
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
                    if (isTouchpadOpen) "Touchpad Panelini Gizle" else "Touchpad Panelini Göster",
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
                            Icon(Icons.Default.FastRewind, contentDescription = "10 sn Geri")
                        }

                        IconButton(
                            onClick = {
                                WebSocketClient.sendMediaCommand("prev")
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Önceki")
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
                            Icon(Icons.Default.PlayArrow, contentDescription = "Oynat / Durdur")
                        }

                        IconButton(
                            onClick = {
                                WebSocketClient.sendMediaCommand("next")
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Sonraki")
                        }

                        IconButton(
                            onClick = {
                                WebSocketClient.sendMediaCommand("forward")
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "10 sn İleri")
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
                            Icon(Icons.Default.VolumeDown, contentDescription = "Ses Kıs")
                        }

                        IconButton(
                            onClick = {
                                WebSocketClient.sendMediaCommand("vol_up")
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Ses Aç")
                        }

                        IconButton(
                            onClick = {
                                WebSocketClient.sendMediaCommand("mute")
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        ) {
                            Icon(Icons.Default.VolumeOff, contentDescription = "Sessiz")
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
                                contentDescription = "Klavye Aç / Kapat"
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
                                    placeholder = { Text("Metin yazın...", style = MaterialTheme.typography.bodySmall) },
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
                                    Icon(Icons.Default.Send, contentDescription = "Metni Gönder")
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
                                    Text("␣ Boşluk", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                                }

                                FilledTonalButton(
                                    onClick = {
                                        WebSocketClient.sendSpecialKey("backspace")
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("⌫ Sil", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                                }

                                FilledTonalButton(
                                    onClick = {
                                        WebSocketClient.sendSpecialKey("enter")
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("⏎ Enter", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                                }

                                FilledTonalButton(
                                    onClick = {
                                        WebSocketClient.sendSpecialKey("esc")
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Esc", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
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
                        "Trackpad",
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
                            if (isSelecting) "Seçim Açık" else "Metin Seç",
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
                            Icon(Icons.Default.Close, contentDescription = "Kapat", modifier = Modifier.size(16.dp))
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
                    if (isSelecting) "📑 Metin Seçimi Aktif — Kaydırarak seçin\nBitirmek için tekrar dokunun"
                    else "Dokun: Tıkla  •  2 Parmak: Scroll & Sağ Tık\nÇift Dokunup Kaydır: Metin Seç",
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
                        "Sol Tık",
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
                        "Çift Tık",
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
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Yukarı Kaydır", modifier = Modifier.size(20.dp))
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
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Aşağı Kaydır", modifier = Modifier.size(20.dp))
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
                        "Sağ Tık",
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
