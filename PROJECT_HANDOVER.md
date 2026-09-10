# 🍎 Mac Remote - Kapsamlı Proje El Kitabı & AI Devir Rehberi (Project Handover)

> **Bu Belgenin Amacı:**
> Bu dosya, projede sıfırdan geliştirilen tüm mimariyi, protokolleri, çözülen kritik işletim sistemi sorunlarını ve derleme süreçlerini içerir. Gelecekte projeyi revize edecek veya geliştirecek olan herhangi bir AI asistanı veya geliştirici, **öncelikle bu dosyayı okuyarak projeyi eksiksiz anlayabilir**.

---

## 📌 1. Proje Özeti
**Mac Remote**, Android akıllı telefonları Mac bilgisayarlar için gelişmiş, sıfıra yakın gecikmeli (ultra-low latency), donanım düzeyinde bir kablosuz uzaktan kumandaya (Trackpad, Klavye, Multimedya Kontrolcü ve Canlı Ekran Aktarımı) dönüştüren bir istemci-sunucu sistemidir.

- **Yerel Ağ (LAN):** mDNS / Zeroconf ile Mac sunucusunu otomatik bulma ve 4 haneli PIN ile hızlı eşleşme.
- **İnternet Erişimi (WAN):** Entegre Ngrok tüneli ile port yönlendirmeye gerek kalmadan dünyanın her yerinden kontrol.
- **Canlı Önizleme:** JPEG yayın akışı ile 4 farklı çözünürlük kademesi (800p - 2200p Ultra HD).

---

## 🏗️ 2. Sistem Mimarisi & Dosya Yapısı

```
remote_mac/
│
├── PROJECT_HANDOVER.md            # Bu el kitabı (Projenin tüm hafızası ve rehberi)
├── README.md                      # Hızlı başlangıç ve özet
├── MacRemote.apk                  # En güncel derlenmiş Android APK (Kullanıma hazır)
│
├── mac_server/                    # macOS Sunucu Uygulaması (Python 3.9 + PyQt6 + CoreGraphics)
│   ├── server.py                  # WebSocket sunucusu, ekran yakalama (mss), JPEG akışı, PIN doğrulama
│   ├── input_controller.py        # macOS CoreGraphics & Quartz API ile donanım düzeyinde fare/klavye kontrolü
│   ├── discovery.py               # Zeroconf (mDNS) yerel ağ servis yayını (_macremote._tcp.local.)
│   ├── gui.py                     # PyQt6 Masaüstü GUI kontrol paneli & Ngrok tünel yönetimi
│   ├── setup.py                   # py2app bağımsız macOS .app paketleme yapılandırması
│   ├── requirements.txt           # Python bağımlılıkları
│   └── dist/
│       └── Mac Remote.app         # Bağımsız macOS uygulama paketi
│
└── android_client/                # Android İstemci Uygulaması (Kotlin + Jetpack Compose + Material 3)
    ├── app/src/main/java/com/example/macremote/
    │   ├── MainActivity.kt        # Ana aktivite, tam ekran yönetimi ve gezinme
    │   ├── network/
    │   │   ├── WebSocketClient.kt # WebSocket bağlantı yönetimi, yeniden bağlanma, veri akışı
    │   │   └── NetworkDiscovery.kt# Android NsdManager ile LAN sunucularını otomatik arama
    │   └── ui/
    │       ├── ConnectionScreen.kt# Otomatik / Manuel IP & Ngrok ve PIN eşleşme ekranı
    │       └── TrackpadScreen.kt  # Ekran önizleme, hareket paneli, tıklama butonları, medya kartı & klavye
    └── jdk-17.0.10+7/             # Gradle derlemesi için yerel bağımsız JDK 17
```

---

## 🔌 3. İletişim Protokolü (WebSocket JSON Spesifikasyonu)

Android istemci ile Mac sunucusu arasında **Port 8765** (veya Ngrok WSS portu) üzerinden çift yönlü WebSocket bağlantısı kurulur.

### 1. Kimlik Doğrulama (Handshake & PIN)
- **Client -> Server:**
  ```json
  {"type": "auth", "pin": "1234"}
  ```
- **Server -> Client:**
  ```json
  {"type": "auth_result", "status": "ok"}
  ```

### 2. Fare Hareketleri & Kaydırma (Mouse & Scroll)
- **İmleç Hareketi:**
  ```json
  {"type": "mouse_move", "dx": 12.5, "dy": -4.2}
  ```
- **Tıklama (Tek / Çift):**
  ```json
  {"type": "mouse_click", "button": "left", "clicks": 1}
  {"type": "mouse_click", "button": "right", "clicks": 1}
  {"type": "mouse_click", "button": "left", "clicks": 2}
  ```
- **İki Parmakla Kaydırma:**
  ```json
  {"type": "scroll", "dx": 0.0, "dy": -5.0}
  ```

### 3. Metin Seçimi ve Sürükleme (Drag & Drop / Text Selection)
- **Sol Tıkı Basılı Tutma (Drag Başlat):**
  ```json
  {"type": "mouse_down", "button": "left"}
  ```
- **Sol Tıkı Bırakma (Drag Bitir):**
  ```json
  {"type": "mouse_up", "button": "left"}
  ```
  *(Not: `mouse_down` sonrasında gönderilen `mouse_move` paketleri macOS'ta `kCGEventLeftMouseDragged` olayı olarak işletilir; böylece metinler seçilir veya pencereler sürüklenir).*

### 4. Klavye ve Özel Tuşlar
- **Normal Karakter Yazma:**
  ```json
  {"type": "key_press", "char": "a"}
  ```
- **Özel Tuşlar:**
  ```json
  {"type": "special_key", "key": "enter"}
  {"type": "special_key", "key": "backspace"}
  {"type": "special_key", "key": "space"}
  {"type": "special_key", "key": "esc"}
  ```

### 5. Multimedya ve Sistem Kontrolleri
- **Medya Komutları:**
  ```json
  {"type": "media", "command": "play_pause"}
  {"type": "media", "command": "next"}
  {"type": "media", "command": "prev"}
  {"type": "media", "command": "rewind"}     // 10 sn geri
  {"type": "media", "command": "forward"}    // 10 sn ileri
  {"type": "media", "command": "vol_up"}
  {"type": "media", "command": "vol_down"}
  {"type": "media", "command": "mute"}
  ```

### 6. Ekran Akışı, Çözünürlük ve İmleç Ayarları
- **Kalite / Çözünürlük Değiştirme:**
  ```json
  {"type": "quality", "level": "low"}     // 800p  - Hızlı / Düşük Veri
  {"type": "quality", "level": "medium"}  // 1200p - Dengeli
  {"type": "quality", "level": "high"}    // 1600p - Net Metin (Varsayılan)
  {"type": "quality", "level": "ultra"}   // 2200p - Kristal Netlik
  ```
- **Önizleme Üzerinde İmleç Göster/Gizle:**
  ```json
  {"type": "toggle_cursor", "show": true}
  ```
- **Görüntü Akışı (Server -> Client):**
  - Mac ekranı `mss` kütüphanesiyle yakalanır, `JPEG` formatında binary frame olarak WebSocket üzerinden gönderilir.
  - Android tarafında `BitmapFactory.decodeByteArray` ile Canvas üzerinde GPU destekli çizilir.

---

## 🧠 4. Çözülen Kritik Sorunlar & Altın Bilgiler (AI Hafızası)

Gelecekte kod üzerinde değişiklik yapılırken aşağıdaki maddelere **kesinlikle dikkat edilmelidir**:

1. **macOS Erişilebilirlik (Accessibility) İzni Çıkmazı:**
   - macOS, `CGEventPost` ve `input_controller` erişimi için *Erişilebilirlik İzni* (`Accessibility`) şart koşar.
   - **Kritik:** `.app` paketi her yeniden derlendiğinde veya değiştirildiğinde macOS eski izni imzası değiştiği için geçersiz sayar ("İzin verilmiş görünür ama kontroller çalışmaz").
   - **Çözüm:** Uygulama `codesign --force --deep --sign -` ile imzalanmalıdır. Gerekirse Sistem Ayarları > Gizlilik ve Güvenlik > Erişilebilirlik menüsünden eski girdi eksi (`-`) butonuyla silinip uygulama yeniden eklenmelidir.

2. **macOS Dock ve Köşe Kısayolları (Hot Corners) Tetiklenmesi:**
   - Yalnızca `CGWarpMouseCursorPosition` çağrısı imleci fiziksel olarak taşır ancak macOS Window Server'a gerçek bir donanım hareketi olduğunu bildirmez (bu yüzden Dock veya ekran köşeleri açılmazdı).
   - **Çözüm:** `input_controller.py` içinde `CGEventCreateMouseEvent` olayı `kCGHIDEventTap` seviyesinde sisteme enjekte edildi. Ayrıca ekran sınırına gelindiğinde küçük bir hız vektörüyle olay post edilerek Dock ve Hot Corner'ların macOS tarafından doğal şekilde açılması sağlandı.

3. **Android'de Çift Karakter Yazma Sorunu:**
   - Klavyede hem TextField'ın `onValueChange` dinleyicisinden anlık harf gönderilip hem de "Gönder" butonuna basılması harflerin 2 defa yazılmasına yol açıyordu.
   - **Çözüm:** Giriş alanı metni hafızada tutar, metin yalnızca "Gönder" butonuna basıldığında veya özel yardımcı tuşlara ("␣ Boşluk", "⌫ Sil", "⏎ Enter") basıldığında tekil olarak sunucuya iletilir.

4. **Ngrok `ERR_NGROK_334` (Endpoint Already Online):**
   - Sunucu kapatıldığında veya çökme durumunda arka plandaki `ngrok` süreci askıda kalarak tünel adresini meşgul ediyordu.
   - **Çözüm:** `gui.py` içinde `start_ngrok` çağrılmadan önce `ngrok.kill()` ile eski süreçler temizlenir; ayrıca `MainWindow.closeEvent` eklenerek pencere kapandığında tünel ve sunucu otomatik olarak tamamen öldürülür.

5. **Android Material 3 Buton Metinlerinin Taşması (Text Overflow):**
   - Material 3 `Button` varsayılan olarak her iki tarafa 24dp dolgu (`contentPadding`) uygular. Bu durum küçük ekranlarda "Çift Tık" yazısının 2 satıra bölünmesine ("Çift" üstte, "Tık" altta) ve sağdaki butonların ekran dışına taşmasına yol açıyordu.
   - **Çözüm:** Üst çubuktaki butonlar Material 3 ikon butonlarına dönüştürüldü. Trackpad altındaki tıklama butonlarına `contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)` ve `maxLines = 1, softWrap = false` uygulandı.

---

## 🛠️ 5. Derleme ve Çalıştırma Rehberi

### Mac Sunucusunu Çalıştırma & Derleme:

1. **Geliştirme Modunda Çalıştırma:**
   ```bash
   cd mac_server
   source venv/bin/activate
   python3 gui.py
   ```

2. **Bağımsız `.app` Olarak Paketleme (py2app):**
   ```bash
   cd mac_server
   source venv/bin/activate
   cp gui.py "dist/Mac Remote.app/Contents/Resources/gui.py"
   rm -rf "/tmp/Mac Remote.app" && cp -R "dist/Mac Remote.app" "/tmp/Mac Remote.app"
   xattr -cr "/tmp/Mac Remote.app" && codesign --force --deep --sign - "/tmp/Mac Remote.app"
   rm -rf "dist/Mac Remote.app" && cp -R "/tmp/Mac Remote.app" "dist/Mac Remote.app"
   cp -R "dist/Mac Remote.app" "/Users/temur/Desktop/Mac Remote.app"
   ```

---

### Android İstemcisini Derleme:

1. **Gradle ile Debug APK Üretme:**
   ```bash
   cd android_client
   export JAVA_HOME="/Users/temur/Desktop/codding/remote_mac/android_client/jdk-17.0.10+7/Contents/Home"
   ./gradlew assembleDebug
   ```

2. **APK'yı Masaüstüne Kopyalama:**
   ```bash
   cp app/build/outputs/apk/debug/app-debug.apk /Users/temur/Desktop/MacRemote.apk
   ```

---

## 🎯 Gelecekteki Geliştirmeler İçin Notlar
- Kullanıcı arayüzü ve donanım kontrol mekanizması tam kararlılığa ulaştırılmıştır.
- Yeni bir tuş veya kısayol eklenirken yukarıda belirtilen WebSocket protokol formatına sadık kalınmalı, `input_controller.py` içine yeni anahtar eşlemesi eklenmeli ve Android tarafında `WebSocketClient.kt` fonksiyonu çağrılmalıdır.
