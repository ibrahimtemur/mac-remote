# Play Store Yayınlama Hazırlık Promptu (3. Adım)

GitHub yayını tamamlandıktan sonra bu promptu kullanarak Android 
uygulamasını Play Store'a yüklemeye hazır hale getirebilirsin. Play 
Store süreci GitHub'dan farklı — imzalama, mağaza listeleme bilgileri, 
gizlilik politikası ve Google'ın inceleme/politika onayı gerektiriyor. 
Bazı adımları (Play Console'da hesap açma, form doldurma, ödeme profili) 
sen manuel yapman gerekecek; ajan bunları sana adım adım anlatacak.

---

## PROMPT (kopyala-yapıştır)

```
Mac Remote projesinin Android istemcisini (android-app) Google Play 
Store'a yayınlamaya hazırlamanı istiyorum. GitHub Release'de sadece 
APK var, ama Play Store için ayrı bir hazırlık gerekiyor. Sırayla 
şunları yap:

## 1. UYGULAMA BİLGİLERİNİ VE PAKET ADINI KONTROL ET
- android-app/app/build.gradle.kts içindeki applicationId'yi kontrol 
  et (örn. com.temur.macremote gibi benzersiz, geri döndürülmüş domain 
  formatında olmalı). Eğer generic/varsayılan bir isimse (com.example.* 
  gibi) bana uygun bir applicationId öner ve onayımı al, sonra değiştir.
  ÖNEMLİ: Bu ID Play Console'a yüklendikten sonra ASLA değiştirilemez.
- targetSdkVersion ve compileSdkVersion'ın Google Play'in güncel 
  zorunlu minimum API seviyesini karşıladığını doğrula (Play Console 
  her yıl yeni uygulamalar için minimum target API seviyesini 
  güncelliyor - bunu web'den güncel haliyle teyit et, tahminde bulunma).

## 2. AAB (ANDROID APP BUNDLE) OLUŞTUR VE PLAY APP SIGNING'E HAZIRLA
- Play Store artık .apk değil .aab (Android App Bundle) formatı 
  bekliyor. build.gradle.kts'de bundle konfigürasyonunun doğru 
  olduğunu kontrol et.
- Play App Signing kullanacağımı varsay (Google'ın önerdiği ve en 
  güvenli yöntem): Ben "upload key" (yükleme anahtarı) oluşturacağım, 
  Google bu upload key ile imzalanmış AAB'yi alıp kendi sakladığı 
  "app signing key" ile son kullanıcıya giden APK'yı üretecek.
- Yerel bir upload keystore oluşturmam için gereken keytool komutunu 
  bana ver (örn. `keytool -genkeypair -v -keystore upload-keystore.jks 
  -alias upload -keyalg RSA -keysize 2048 -validity 10000`), ben bunu 
  çalıştıracağım ve şifreleri güvenli bir yerde saklayacağım.
- `./gradlew bundleRelease` komutuyla imzalı bir .aab dosyası üretecek 
  şekilde gradle yapılandırmasını ayarla (imzalama bilgilerini 
  gradle.properties yerine ortam değişkenlerinden okuyacak şekilde 
  kur, ASLA şifreleri koda gömme).
- Bu upload keystore'un android-app/.gitignore'da zaten hariç 
  tutulduğunu doğrula (önceki .gitignore'da *.jks zaten var, teyit et).

## 3. GEREKLİ YASAL/POLİTİKA DÖKÜMANLARINI HAZIRLA
- Play Console, HERHANGİ BİR uygulama için artık bir "Gizlilik 
  Politikası" (Privacy Policy) URL'si zorunlu tutuyor. Bu uygulama 
  ağ üzerinden ekran/girdi verisi taşıdığı için özellikle önemli. Bana 
  basit ama yeterli bir gizlilik politikası metni (Türkçe ve İngilizce) 
  taslağı hazırla; içinde şunlar olsun:
  - Uygulamanın hangi verileri işlediği (yerel ağ/Ngrok üzerinden 
    ekran görüntüsü ve girdi komutları - sunucuya gönderilen, 
    saklanmayan veriler)
  - Verilerin üçüncü taraflarla paylaşılmadığı
  - Kullanıcının PIN doğrulaması dışında bir hesap/kişisel veri 
    toplanmadığı
  - İletişim için bir e-posta adresi (benim vereceğim)
- Bu metni GitHub Pages üzerinden barındırmam için basit bir HTML 
  sayfası olarak da hazırla (ör. docs/privacy-policy.html), böylece 
  GitHub Pages ile ücretsiz bir URL elde edebilirim - bu süreç için 
  gerekli adımları (Settings > Pages > Deploy from branch) bana anlat.

## 4. "VERİ GÜVENLİĞİ" (DATA SAFETY) FORMU İÇİN İÇERİK HAZIRLA
- Play Console'daki "Data Safety" formunu doldururken kullanmam için 
  bana bir kontrol listesi hazırla: bu uygulama hangi veri 
  kategorilerini topluyor/iletiyor (örn. "Cihaz veya diğer kimlikler" 
  toplanmıyor, ama "Uygulama etkinliği" ekran verisi gerçek zamanlı 
  iletiliyor ama saklanmıyor gibi). Ben bu listeye bakarak formu Play 
  Console'da elle dolduracağım (form üçüncü parti bir arayüz, ajan 
  bunu otomatik dolduramaz).

## 5. HASSAS İZİNLER VE POLİTİKA UYUMU İÇİN RİSK ANALİZİ
- android-app/app/src/main/AndroidManifest.xml dosyasındaki tüm 
  izinleri (permissions) listele ve her biri için:
  - Google Play politikalarına göre "hassas izin" (sensitive 
    permission) sayılıp sayılmadığını belirt (örn. Accessibility 
    Service, Foreground Service, Internet, Network State vb.)
  - Hassas izin varsa, Play Console'da "İzin Bildirimi" 
    (Permissions Declaration) formunda nasıl gerekçelendirilmesi 
    gerektiğine dair bana taslak metin hazırla. Özellikle bu uygulama 
    bir "uzaktan kontrol" uygulaması olduğu için Google'ın bu tür 
    uygulamalara bakışını (Erişilebilirlik/Accessibility API kötüye 
    kullanım politikaları vb.) web'den güncel haliyle araştır ve 
    bana özetle - reddedilme riskini azaltmak için nelere dikkat 
    etmem gerektiğini söyle.

## 6. UYGULAMA MAĞAZA LİSTELEME (STORE LISTING) İÇERİĞİ HAZIRLA
- Play Console mağaza sayfası için gereken şu metinleri hazırla:
  - Kısa açıklama (80 karakter sınırı)
  - Uzun açıklama (4000 karakter sınırı, Türkçe ve İngilizce)
  - Gerekli görsel boyutlarını listele (ikon 512x512, feature graphic 
    1024x500, en az 2 ekran görüntüsü telefon için) ve screenshots/ 
    klasöründeki mevcut görsellerin bu boyutlara uyup uymadığını 
    kontrol et, uymuyorsa bana hangi boyutta yeniden hazırlamam 
    gerektiğini söyle.

## 7. YAYIN STRATEJİSİ ÖNERİSİ
- Google Play'in yeni geliştirici hesapları için zorunlu tuttuğu 
  "kapalı test" (closed testing, en az 12-20 test kullanıcısıyla 14 
  gün) sürecinin güncel kurallarını web'den araştır ve bana adım adım 
  bu süreci nasıl yöneteceğimi anlat (bu kural sık değişiyor, 
  tahmine dayanma).
- İlk sürüm için production'a değil, önce "Kapalı Test" (Closed 
  Testing) veya "Açık Test" (Open Testing) track'ine yüklemeyi öner, 
  sonra production'a terfi sürecini anlat.

## 8. ÖZET RAPOR
İşin sonunda bana:
- Hazır olan dosyaların listesi (privacy policy, data safety 
  checklist, store listing metinleri)
- Play Console'da MANUEL olarak benim yapmam gereken adımların 
  numaralı bir listesi (hesap açma, 25$ tek seferlik ücret, form 
  doldurma, AAB yükleme vb.)
- Riskli/dikkat edilmesi gereken noktaların özeti (özellikle 
  "uzaktan kontrol" uygulaması olması nedeniyle politika reddi riski)

Play Console hesabı açma, ödeme bilgisi girme, formu gönderme gibi 
işlemleri SEN YAPAMAZSIN - bunlar tarayıcı üzerinden benim yapmam 
gereken adımlar. Senin görevin bana gereken TÜM içeriği ve doğru 
sırayı hazırlamak, böylece ben sadece kopyala-yapıştır yaparak süreci 
hızlıca tamamlayabileyim.
```

---

### Bilmen gereken önemli noktalar

- **Play Console tek seferlik 25$ kayıt ücreti** gerektiriyor — bunu 
  ajan senin yerine ödeyemez, kendi hesabınla yapman gerekiyor.
- **"Uzaktan kontrol" uygulamaları Google Play'de daha sıkı incelemeye 
  tabi tutulabilir**, özellikle Accessibility Service kullanıyorsan. 
  Senin uygulaman Mac'i kontrol ediyor (tam tersi değil), yani Android 
  tarafında muhtemelen hassas bir izin yok — ama yine de Manifest'i 
  agent'a kontrol ettirmek doğru bir adım.
- İlk başvurunda **yeni geliştirici hesapları için Google'ın "kapalı 
  test" zorunluluğu** var; bu süreç zaman zaman değişiyor, promptun 
  içine güncel bilgiyi web'den araştırması için özellikle ekledim.
- Gizlilik politikası ve veri güvenliği formu **her uygulama için 
  zorunlu**, atlanamaz.
