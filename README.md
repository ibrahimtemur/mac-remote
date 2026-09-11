<div align="center">

# 🍎 Mac Remote

### Ultra-Low Latency Wireless Trackpad, Keyboard & Live Screen Streamer for Mac from Android

[![GitHub Release](https://img.shields.io/github/v/release/ibrahimtemur/mac-remote?style=for-the-badge&color=blue)](https://github.com/ibrahimtemur/mac-remote/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)
[![CI Build](https://github.com/ibrahimtemur/mac-remote/actions/workflows/build.yml/badge.svg)](https://github.com/ibrahimtemur/mac-remote/actions/workflows/build.yml)
[![Platform](https://img.shields.io/badge/Platform-macOS%20%7C%20Android-black?style=for-the-badge&logo=apple)](https://github.com/ibrahimtemur/mac-remote)

<br/>

Turn your Android smartphone into a high-precision, hardware-level remote trackpad, media controller, keyboard, and crystal-clear display mirror for macOS — over both Local Wi-Fi (LAN) and Internet (WAN).

[Features](#-key-features) • [How It Works](#-how-it-works) • [Installation](#-installation) • [Developer Guide](#-developer-guide) • [Security](#-security)

</div>

---

## 📸 Screenshots

<div align="center">
  <h3>🇬🇧 English Interface</h3>
  <table>
    <tr>
      <td align="center"><b>1. Android Discovery & Pairing</b></td>
      <td align="center"><b>2. Trackpad, Mirror & Quality Tiers</b></td>
      <td align="center"><b>3. macOS Server Control Panel</b></td>
    </tr>
    <tr>
      <td align="center" valign="top"><img src="screenshots/android_connection_en.png" alt="Android Connection Screen (EN)" width="230"/></td>
      <td align="center" valign="top"><img src="screenshots/android_trackpad_en.png" alt="Android Trackpad & Live Screen (EN)" width="230"/></td>
      <td align="center" valign="top"><img src="screenshots/mac_server_en.png" alt="macOS Server GUI (EN)" width="280"/></td>
    </tr>
  </table>

  <h3>🇹🇷 Türkçe Arayüz</h3>
  <table>
    <tr>
      <td align="center"><b>1. Android Cihaz Keşfi & Bağlantı</b></td>
      <td align="center"><b>2. Trackpad, Ekran & Kalite Menüsü</b></td>
      <td align="center"><b>3. macOS Sunucu Kontrol Paneli</b></td>
    </tr>
    <tr>
      <td align="center" valign="top"><img src="screenshots/android_connection_tr.png" alt="Android Bağlantı Ekranı (TR)" width="230"/></td>
      <td align="center" valign="top"><img src="screenshots/android_trackpad_tr.png" alt="Android Trackpad & Canlı Ekran (TR)" width="230"/></td>
      <td align="center" valign="top"><img src="screenshots/mac_server_tr.png" alt="macOS Sunucu GUI (TR)" width="280"/></td>
    </tr>
  </table>
</div>

---

## ✨ Key Features

- 🖱️ **Hardware-Level Trackpad Emulation:**
  - **Fluid Cursor Navigation:** macOS CoreGraphics synthetic event dispatching with sub-pixel precision.
  - **Natural Multi-Touch Gestures:** 1-finger tap (left click), 1-finger long press or 2-finger tap (right click), and 2-finger fluid scrolling (vertical & horizontal).
  - **Full Text Selection & Drag-and-Drop:** Native `kCGEventLeftMouseDragged` support via double-tap-and-drag gesture or the dedicated **"Select Text"** toggle button.
  - **Dock & Hot Corners Triggering:** Engineered with cursor velocity boundary events to seamlessly summon the macOS Dock and Mission Control / Hot Corners.
- 📺 **Dynamic Real-Time Screen Mirroring:**
  - Ultra-fast JPEG frame streaming powered by native ScreenCapture API (`mss`).
  - **4 Dynamic Resolution Tiers:** Switch dynamically on the fly between **800p** (Fast / Low Data), **1200p** (Balanced), **1600p** (Crisp Text / Coding), and **2200p** (Ultra HD Crystal Clear).
  - Fullscreen & auto-rotating landscape layout with floating touchpad drawer.
  - Interactive cursor overlay toggle on the preview canvas.
- 🎵 **Dedicated Multimedia & System Control Bar:**
  - Play/Pause, Next Track, Previous Track.
  - 10-second fast-forward and 10-second rewind for media and video players.
  - Native system volume buttons (Volume Up, Volume Down, Mute).
- ⌨️ **Expandable Virtual Keyboard:**
  - Integrated soft keyboard with quick helper buttons: `␣ Space`, `⌫ Backspace`, `⏎ Enter`, and `Esc`.
  - Glitch-free, single-stroke text delivery (no double-character bugs).
- 🌐 **Zero-Config Networking & Remote Access:**
  - **Local Network (LAN):** Automatic server discovery via mDNS / Zeroconf (`_macremote._tcp.local.`).
  - **Internet Access (WAN):** Integrated Ngrok encrypted TLS tunnel option — access and control your Mac from anywhere in the world without router port forwarding.
  - **Dynamic 4-Digit PIN Security:** Fast, tamper-resistant handshake authentication.

---

## 🏗️ Architecture & How It Works

```
┌───────────────────────────┐                     ┌───────────────────────────┐
│     Android Client        │                     │       macOS Server        │
│  (Kotlin/Jetpack Compose) │                     │     (Python 3.9+/PyQt6)   │
├───────────────────────────┤                     ├───────────────────────────┤
│ • NsdManager (mDNS)       │ <── Auto-Discover ─ │ • Zeroconf Publisher      │
│ • OkHttp WebSocket Client │                     │ • Asyncio WebSocket Server│
│ • Compose Surface Canvas  │ ── JSON Commands ─> │ • CoreGraphics (Quartz)   │
│ • Local Haptics & Gestures│ <── JPEG Frames ─── │ • Native 'mss' Grabber    │
└───────────────────────────┘                     └───────────────────────────┘
```

1. **Discovery & Pairing:**
   - When the macOS server starts, it broadcasts its presence across the local network via Zeroconf (`_macremote._tcp.local.`).
   - The Android app discovers the host automatically using Android's `NsdManager`.
   - The client sends an authentication handshake containing the 4-digit PIN displayed on the Mac GUI.
2. **Input Injection:**
   - Motion and touch events are transmitted over low-overhead JSON WebSocket packets.
   - The server converts these into macOS CoreGraphics Quartz events (`CGEventCreateMouseEvent`, `CGEventPost` at `kCGHIDEventTap` level), providing true system-wide hardware emulation.
3. **Screen Mirroring:**
   - Screen frames are captured at the selected target resolution and compressed into JPEG memory buffers, transmitted as binary WebSocket frames for direct GPU rendering.

---

## 📥 Installation

### 🍏 macOS (Server)
1. Navigate to the [Releases](https://github.com/ibrahimtemur/mac-remote/releases) page and download the latest `MacRemote-macOS.zip`.
2. Extract the archive and drag **Mac Remote.app** into your `/Applications` folder.
3. **Important Accessibility Permission:**
   - Open **macOS System Settings** > **Privacy & Security** > **Accessibility**.
   - Ensure **Mac Remote** is checked / allowed. *(This enables synthetic cursor and keyboard control).*
4. Launch **Mac Remote**, click **Start Server**, and note the 4-digit PIN.

### 🤖 Android (Client)
1. Download the latest `MacRemote-Android.apk` from the [Releases](https://github.com/ibrahimtemur/mac-remote/releases) page.
2. Open the downloaded file on your Android device. If prompted, allow *"Install unknown apps"* for your browser or file manager.
3. Open **Mac Remote**, tap your Mac from the auto-discovered list (or enter the IP / Ngrok URL manually), enter the 4-digit PIN, and connect.

---

## 💻 Developer Guide (Build from Source)

### Repository Structure
```
├── mac-app/           # macOS desktop server (Python 3.9+, PyQt6, py2app)
├── android-app/       # Android client app (Kotlin, Jetpack Compose, Material 3)
├── .github/           # GitHub Actions CI/CD workflows
└── scripts/           # DevOps and release automation scripts
```

### 1. Building the macOS Server
```bash
# Clone the repository
git clone https://github.com/ibrahimtemur/mac-remote.git
cd mac-remote/mac-app

# Create virtual environment and install dependencies
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Run in development mode
./run.sh

# Or build standalone macOS .app bundle
python3 setup.py py2app
```

### 2. Building the Android Client
```bash
cd mac-remote/android-app

# Build debug APK using Gradle wrapper
./gradlew assembleDebug

# Output APK located at:
# android-app/app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔒 Security

Because Mac Remote provides system-level remote access:
- Dynamic 4-digit PIN verification protects every session.
- Local Wi-Fi communication stays strictly inside your private network.
- WAN connections use encrypted HTTPS / WSS through Ngrok TLS endpoints.
- Read our full [Security Policy (SECURITY.md)](SECURITY.md) for responsible disclosure guidelines.

---

## 🗺️ Roadmap

- [x] Multi-touch gestures (Tap, Right Click, 2-Finger Scroll, Drag Selection)
- [x] Dynamic screen quality tiers (800p to 2200p)
- [x] WAN access via Ngrok
- [ ] Bluetooth LE fallback connection for offline environments
- [ ] Multi-monitor switcher on macOS
- [ ] Biometric (Fingerprint) PIN quick-unlock on Android
- [ ] Audio streaming from Mac to Android

---

## 🤝 Contributing

Contributions are warmly welcomed! Please read our [Contributing Guidelines (CONTRIBUTING.md)](CONTRIBUTING.md) and [Code of Conduct (CODE_OF_CONDUCT.md)](CODE_OF_CONDUCT.md) before submitting a Pull Request.

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

<div align="center">
  <sub>Made with ❤️ for seamless Mac & Android interoperability.</sub>
</div>
