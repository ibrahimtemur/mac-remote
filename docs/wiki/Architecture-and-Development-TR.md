# 🏗️ Mimari & Geliştirici Rehberi

<p align="center">
  <a href="Architecture-and-Development"><b>🇬🇧 English</b></a> •
  <a href="Architecture-and-Development-TR"><b>🇹🇷 Türkçe</b></a>
</p>

Bu belge, Mac Remote projesine katkıda bulunmak veya kaynak koddan derlemek isteyen geliştiriciler için sistem mimarisini ve protokollerini açıklar.

---

## 🏛️ Sistem Mimarisi

```mermaid
graph TD
    subgraph Android İstemcisi ["Android İstemcisi (Jetpack Compose)"]
        UI[Dokunmatik Yüzey / Hareketler]
        Discovery[DiscoveryManager (mDNS NsdManager)]
        WSClient[WebSocketClient (OkHttp)]
        UI --> WSClient
        Discovery --> WSClient
    end

    subgraph macOS Sunucusu ["macOS Sunucusu (PyQt6 + Asyncio)"]
        WSServer[WebSocket Sunucusu (Port 8080)]
        mDNS[Zeroconf Servisi (_macremote._tcp)]
        InputCtrl[Girdi Yöneticisi (Quartz & CoreGraphics)]
        ScreenCap[Ekran Yakalama (MSS / CoreGraphics)]
        Tunnel[PyNgrok Köprüsü]
        
        WSServer --> InputCtrl
        WSServer --> ScreenCap
        Tunnel -.-> WSServer
    end

    WSClient <==>|JSON Mesajları + JPEG Kareleri| WSServer
```

---

## 📡 WebSocket Protokol Formatları

İstemci ve sunucu arasındaki tüm kontroller WebSocket üzerinden hafif JSON paketleriyle iletilir:

### 1. Bağıl Fare Hareketi
```json
{
  "type": "mouse_move",
  "dx": 12.5,
  "dy": -4.2
}
```

### 2. Mutlak İmleç Konumu
```json
{
  "type": "absolute_move",
  "x": 0.452,
  "y": 0.810
}
```

### 3. Fare Tıklamaları
```json
{
  "type": "mouse_click",
  "button": "left",
  "clicks": 1
}
```

### 4. Kaydırma
```json
{
  "type": "scroll",
  "dx": 0.0,
  "dy": 15.0
}
```

### 5. Klavye Girişi
```json
{
  "type": "key_press",
  "key": "enter"
}
```

---

## 🛠️ Kaynak Koddan Derleme

### Android İstemcisi
```bash
cd android-app
export JAVA_HOME="/path/to/jdk-17"

# Debug APK derleme
./gradlew assembleDebug

# Release App Bundle (AAB) derleme
./gradlew bundleRelease
```

### macOS Sunucusu
```bash
cd mac-app
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Doğrudan çalıştırma
python3 gui.py

# Bağımsız .app paketi oluşturma
python3 setup.py py2app
```
