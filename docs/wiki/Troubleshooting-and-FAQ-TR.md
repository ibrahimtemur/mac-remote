# 🔧 Sorun Giderme & Sıkça Sorulan Sorular

<p align="center">
  <a href="Troubleshooting-and-FAQ"><b>🇬🇧 English</b></a> •
  <a href="Troubleshooting-and-FAQ-TR"><b>🇹🇷 Türkçe</b></a>
</p>

Mac Remote kullanırken karşılaşabileceğiniz en yaygın sorular ve çözümleri:

---

## ❓ Sıkça Sorulan Sorular

### S1: Telefonum Mac'imi otomatik olarak bulamıyor.
**Kontrol Listesi:**
1. **Aynı Wi-Fi Kontrolü:** Mac'inizin ve telefonunuzun **birebir aynı Wi-Fi ağına** bağlı olduğundan emin olun.
2. **Misafir Ağı / AP İzolasyonu:** Halka açık, otel veya misafir Wi-Fi ağlarında cihazların birbiriyle konuşmasını engelleyen *AP İzolasyonu* açık olabilir. Bu durumda **Ngrok** modunu kullanın.
3. **macOS Güvenlik Duvarı:**
   - **Sistem Ayarları > Ağ > Güvenlik Duvarı** bölümüne gidin.
   - `Mac Remote.app` için gelen bağlantılara izin verildiğinden emin olun.
4. **Manuel Bağlantı:** Mac'inizin yerel IP adresiyle doğrudan bağlanabilirsiniz:
   - Mac'inizde IP adresinize bakın (örn: `192.168.1.50`).
   - Android uygulamasında Manuel Bağlantı alanına `ws://192.168.1.50:8080` yazın.

---

### S2: Bağlantı kuruldu ancak imleç hareket etmiyor.
**Sebep:** macOS Erişilebilirlik izni eksik veya pasif kalmış.
**Çözüm:**
1. **Sistem Ayarları > Gizlilik ve Güvenlik > Erişilebilirlik** bölümüne gidin.
2. `Mac Remote` listelenmişse anahtarı **kapatıp tekrar açın**.
3. Listelenmemişse `+` butonuna basarak `/Applications/Mac Remote.app` dosyasını ekleyin.
4. Mac Remote uygulamasını yeniden başlatın (`✓ Erişilebilirlik İzni: Verildi` görünmelidir).

---

### S3: Ngrok hata veriyor (`Ngrok Error: ...`).
**Olası Nedenler:**
1. **AuthToken Eksik:** [ngrok.com](https://ngrok.com) hesabınızdan aldığınız belirtecin geçerli olduğunu kontrol edin.
2. **Birden Fazla Tünel:** Ücretsiz Ngrok hesabı aynı anda 1 aktif tünele izin verir. Bilgisayarda başka bir ngrok işlemi açıksa kapatın.

---

### S4: Verilerim güvende mi? Tuş vuruşlarım kaydediliyor mu?
**Cevap: Kesinlikle güvende.**
- Yerel bağlantılar doğrudan modeminiz üzerinden cihazlar arası (P2P) gerçekleşir.
- Hiçbir analitik, izleyici veya harici sunucuya veri gönderilmez.
- Ngrok kullanıldığında trafik uçtan uca TLS (`wss://`) ile şifrelenir.
- Tüm kaynak kodları MIT lisansı altında tamamen açıktır.
