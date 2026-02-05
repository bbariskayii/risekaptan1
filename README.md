# RiseKaptan - Profesyonel RTP Eklentisi

## 📋 Özellikler

### ✅ Temel Özellikler
- **Rastgele Işınlanma (RTP)** - Oyuncuları güvenli rastgele konumlara ışınlar
- **Çoklu Dünya Desteği** - Farklı dünyalara RTP yapabilme
- **Grafik Menü Sistemi** - Kullanıcı dostu GUI menü
- **Cooldown Sistemi** - İzin tabanlı bekleme süreleri
- **Ekonomi Entegrasyonu** - Vault, CoinsEngine, PlayerPoints desteği
- **Veritabanı Desteği** - MySQL ve SQLite

### 🎭 RPG Yolculuk Sistemi
- Oyunculara sinematik yolculuk deneyimi
- Kaptan NPC ile diyaloglar
- Ses, particle ve efekt sistemi
- İlk seferlik veya her seferinde aktif edilebilir
- Citizens ve FancyNpcs desteği

### ⚔️ Galeyan Arena Sistemi
- Belirli alanlarda toplu RTP
- Geri sayım sistemi
- Grup halinde aynı konuma ışınlanma
- PvP koruma sistemi
- WorldEdit ile alan oluşturma

### 🔐 Güvenlik ve Koruma
- Dünya bazlı izin sistemi
- İzinsiz dünyalardan otomatik atılma
- GriefPrevention, Towny, Lands entegrasyonu
- PvP koruma mekaniği

### 🎨 Özelleştirme
- HEX renk kodu desteği
- ItemsAdder ve Oraxen desteği
- Custom Model Data desteği
- Tüm mesajlar düzenlenebilir
- Sesler, parçacıklar, efektler ayarlanabilir

### 📊 PlaceholderAPI
- `%kaptan_cooldown%` - Kalan cooldown (saniye)
- `%kaptan_cooldown_formatted%` - Formatlanmış cooldown
- `%kaptan_traveling%` - Yolculuk durumu
- `%kaptan_has_pvp_protection%` - PvP koruma durumu
- `%kaptan_galeyan_<alan>_countdown%` - Galeyan geri sayımı
- `%kaptan_galeyan_<alan>_players%` - Alandaki oyuncu sayısı

## 📦 Kurulum

### Gereksinimler
- Spigot/Paper 1.16.5 - 1.21.x
- Java 8 veya üzeri
- Vault (ekonomi için)
- PlaceholderAPI (opsiyonel)

### Adımlar
1. `RiseKaptan.jar` dosyasını `plugins` klasörüne atın
2. Sunucuyu başlatın
3. `plugins/RiseKaptan/` klasöründeki ayar dosyalarını düzenleyin
4. `/kaptanadmin reload` komutuyla yeniden yükleyin

## 🔨 Derleme

### Maven ile Derleme
```bash
cd RiseKaptan
mvn clean package
```

Derlenmiş JAR dosyası `target/RiseKaptan.jar` konumunda oluşacaktır.

### Gerekli Bağımlılıklar
- Spigot API 1.16.5
- Vault API (opsiyonel)
- PlaceholderAPI (opsiyonel)
- WorldEdit (Galeyan alan oluşturma için)
- Citizens veya FancyNpcs (RPG travel için)

## 📚 Komutlar

### Oyuncu Komutları
- `/kaptan` - Kaptan menüsünü açar
- `/rtp` - Bulunduğun dünyada rastgele ışınlanma yapar

### Admin Komutları
- `/kaptanadmin reload` - Eklentiyi yeniden yükler
- `/kaptanadmin rtp <oyuncu> <dünya>` - Oyuncuyu zorla RTP yapar
- `/kaptanadmin travel setplayer` - RPG yolculuk konumunu ayarlar
- `/kaptanadmin travel setnpc` - RPG NPC konumunu ayarlar
- `/kaptanadmin galeyan setarea <isim>` - Galeyan alanı oluşturur (WorldEdit seçimi gerekli)
- `/kaptanadmin galeyan delete <isim>` - Galeyan alanını siler
- `/kaptanadmin galeyan list` - Tüm Galeyan alanlarını listeler
- `/kaptanadmin galeyan interval <isim> <saniye>` - Alan aralığını ayarlar

## 🔑 İzinler

### Temel İzinler
- `risekaptan.use` - Kaptan menüsünü kullanma
- `risekaptan.admin` - Admin komutlarına erişim
- `risekaptan.bypass.cooldown` - Cooldown'ı atlama
- `risekaptan.bypass.cost` - Ücreti atlama

### Dünya İzinleri
- `risekaptan.world.<dünya>` - Belirli dünyaya erişim (config'de ayarlanır)

### Cooldown İzinleri
- `risekaptan.cooldown.vip` - VIP cooldown süresi
- `risekaptan.cooldown.mvp` - MVP cooldown süresi
- `risekaptan.cooldown.premium` - Premium cooldown süresi

### Ücret İzinleri
- `risekaptan.cost.vip` - VIP fiyatlandırma
- `risekaptan.cost.mvp` - MVP fiyatlandırma
- `risekaptan.cost.premium` - Premium fiyatlandırma

## ⚙️ Yapılandırma Dosyaları

### config.yml
- Ana yapılandırma
- Veritabanı ayarları
- Cooldown ve maliyet ayarları
- Devre dışı dünyalar

### messages.yml
- Tüm plugin mesajları
- HEX renk kodu desteği
- Türkçe dilinde

### menu.yml
- GUI menü tasarımı
- İtem pozisyonları
- Lore ve isimler
- Custom Model Data

### worlds.yml
- Dünya bazlı RTP ayarları
- Maksimum X ve Z koordinatları
- Dünya maliyetleri
- İzin tabanlı fiyatlandırma

### rpg-travel.yml
- RPG yolculuk sistemi
- Kaptan diyalogları
- Efektler ve sesler
- NPC konumları

### galeyan.yml
- Galeyan sistem ayarları
- Geri sayım duyuruları
- Varsayılan değerler

## 🎯 Kullanım Örnekleri

### Galeyan Alanı Oluşturma
1. WorldEdit ile bir alan seç (`//wand`, `//pos1`, `//pos2`)
2. `/kaptanadmin galeyan setarea arena1` komutunu kullan
3. `/kaptanadmin galeyan interval arena1 300` ile 5 dakikalık aralık ayarla

### RPG Travel Ayarlama
1. Oyuncuların ışınlanacağı alana git
2. `/kaptanadmin travel setplayer` komutunu kullan
3. NPC'nin spawn olacağı yere git
4. `/kaptanadmin travel setnpc` komutunu kullan

### Dünya İzni Oluşturma
config.yml'de:
```yaml
world-permissions:
  enabled: true
  worlds:
    mining_world: risekaptan.world.mining
```

Ardından oyunculara `risekaptan.world.mining` iznini verin.

## 🐛 Sorun Giderme

### Veritabanı Bağlantı Hatası
- MySQL kullanıyorsanız, bağlantı bilgilerini kontrol edin
- SQLite kullanmayı deneyin (varsayılan)

### WorldEdit Bulunamadı
- WorldEdit eklentisinin yüklü olduğundan emin olun
- Sadece Galeyan alan oluşturma için gereklidir

### NPC Görünmüyor
- Citizens veya FancyNpcs eklentilerinden birini yükleyin
- RPG Travel sisteminde NPC opsiyoneldir

## 📝 Geliştirici Notları

### Proje Yapısı
```
RiseKaptan/
├── src/main/java/com/risekaptan/
│   ├── RiseKaptan.java          # Ana plugin sınıfı
│   ├── commands/                 # Komut işleyicileri
│   ├── config/                   # Konfig yöneticisi
│   ├── database/                 # Veritabanı yönetimi
│   ├── galeyan/                  # Galeyan sistem
│   ├── hooks/                    # Plugin entegrasyonları
│   ├── listeners/                # Event dinleyicileri
│   ├── menu/                     # GUI menü sistemi
│   ├── placeholder/              # PlaceholderAPI
│   ├── rpg/                      # RPG Travel sistemi
│   ├── rtp/                      # RTP yöneticisi
│   └── utils/                    # Yardımcı sınıflar
└── src/main/resources/
    ├── plugin.yml
    ├── config.yml
    ├── messages.yml
    ├── menu.yml
    ├── worlds.yml
    ├── rpg-travel.yml
    └── galeyan.yml
```

### API Kullanımı
```java
// RTP yöneticisine erişim
RTPManager rtpManager = RiseKaptan.getInstance().getRTPManager();

// Oyuncuyu belirli dünyaya RTP yap
rtpManager.performRTP(player, "world", false);

// Galeyan alanı oluştur
GaleyanManager galeyanManager = RiseKaptan.getInstance().getGaleyanManager();
galeyanManager.createArea(name, pos1, pos2, targetWorld, 300, 10);
```

## 📄 Lisans
Bu eklenti özel olarak sizin için geliştirilmiştir.

## 👤 Geliştirici
**RiseKaptan Development Team**

---

**Not**: Bu eklenti Minecraft 1.16.5'ten 1.21.x'e kadar tüm sürümleri destekler.
