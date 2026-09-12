# 🏗️ Architecture & Developer Guide

<p align="center">
  <a href="Architecture-and-Development"><b>🇬🇧 English</b></a> •
  <a href="Architecture-and-Development-TR"><b>🇹🇷 Türkçe</b></a>
</p>


This document details the internal architecture, network protocols, and build instructions for contributors and developers working on Mac Remote.

---

## 🏛️ System Overview

```mermaid
graph TD
    subgraph Android App ["Android Client (Jetpack Compose)"]
        UI[Touchpad Screen / Gestures]
        Discovery[DiscoveryManager (mDNS NsdManager)]
        WSClient[WebSocketClient (OkHttp)]
        UI --> WSClient
        Discovery --> WSClient
    end

    subgraph macOS Host ["macOS Server (PyQt6 + Asyncio)"]
        WSServer[WebSocket Server (Port 8080)]
        mDNS[Zeroconf Service (_macremote._tcp)]
        InputCtrl[Input Controller (Quartz & CoreGraphics)]
        ScreenCap[Screen Capture (MSS / CoreGraphics)]
        Tunnel[PyNgrok Bridge]
        
        WSServer --> InputCtrl
        WSServer --> ScreenCap
        Tunnel -.-> WSServer
    end

    WSClient <==>|JSON Messages + JPEG Frames| WSServer
```

---

## 📡 WebSocket Protocol Specification

All control communications between client and server occur over a persistent WebSocket connection using lightweight JSON payloads:

### 1. Relative Mouse Move
```json
{
  "type": "mouse_move",
  "dx": 12.5,
  "dy": -4.2
}
```

### 2. Absolute Cursor Placement
```json
{
  "type": "absolute_move",
  "x": 0.452,
  "y": 0.810
}
```
*(Coordinates are normalized percentages between `0.0` and `1.0`).*

### 3. Mouse Clicks
```json
{
  "type": "mouse_click",
  "button": "left",
  "clicks": 1
}
```
*(Supported `button` values: `"left"`, `"right"`, `"double"`).*

### 4. Scrolling
```json
{
  "type": "scroll",
  "dx": 0.0,
  "dy": 15.0
}
```

### 5. Keyboard Input
```json
{
  "type": "key_press",
  "key": "enter"
}
```
*(Or `"type": "text", "text": "Hello World"` for string buffers).*

### 6. Media Actions
```json
{
  "type": "media_key",
  "action": "play_pause"
}
```
*(Actions: `"play_pause"`, `"prev"`, `"next"`, `"vol_up"`, `"vol_down"`, `"mute"`, `"forward_10s"`, `"rewind_10s"`).*

---

## 🛠️ Building From Source

### Android Client
**Requirements:** JDK 17, Android SDK API 36.
```bash
cd android-app
export JAVA_HOME="/path/to/jdk-17"

# Build debug APK
./gradlew assembleDebug

# Build release App Bundle (AAB)
./gradlew bundleRelease
```

### macOS Application
**Requirements:** Python 3.9+, macOS 12+, Xcode Command Line Tools.
```bash
cd mac-app
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Run directly
python3 gui.py

# Package into standalone .app
python3 setup.py py2app
```
