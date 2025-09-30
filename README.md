# 📱 DayPlanner - Kişisel Not ve Finans Yönetimi Uygulaması

<div align="center">
  <img src="app/src/main/res/drawable/ic_kite_star.xml" alt="DayPlanner Logo" width="100" height="100">
  
  **Modern, Güvenli ve Kullanıcı Dostu Not Yönetimi**
  
  [![Version](https://img.shields.io/badge/version-3.0-blue.svg)](https://github.com/srcnbllc/Dayplanner/releases/tag/v3.0)
  [![Android](https://img.shields.io/badge/Android-24%2B-green.svg)](https://developer.android.com/)
  [![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple.svg)](https://kotlinlang.org/)
  [![License](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)
</div>

---

## 🌟 Özellikler

### 📝 Not Yönetimi
- **Akıllı Notlar**: Kategorize edilmiş not sistemi
- **Şifreleme**: AES-256 ile güvenli şifreleme
- **Pinleme**: Önemli notları üst sırada tutma
- **Arama**: Gelişmiş arama ve filtreleme
- **Çöp Kutusu**: 30 günlük geri yükleme sistemi

### 💰 Finans Yönetimi
- **Dashboard**: Gelir, gider ve bakiye özeti
- **Dinamik Formlar**: Gelir/gider türlerine göre özelleştirilmiş alanlar
- **Filtreleme**: Tarih aralığı ve kategori bazlı filtreleme
- **Raporlama**: Detaylı finansal analizler
- **Hatırlatmalar**: Ödeme tarihi bildirimleri

### 🔐 Güvenlik
- **Biyometrik Kimlik Doğrulama**: Parmak izi ve yüz tanıma
- **Şifreli Notlar**: AES-256 şifreleme
- **Güvenli Saklama**: Room Database ile yerel şifreleme

### 🎨 Modern Tasarım
- **Kite Design**: Özel tasarım sistemi
- **Material Design 3**: Modern Android tasarım dili
- **Dark/Light Theme**: Otomatik tema desteği
- **Responsive UI**: Tüm ekran boyutlarına uyumlu

---

## 📱 Ekran Görüntüleri

<div align="center">
  <img src="screenshots/main_screen.png" alt="Ana Ekran" width="200">
  <img src="screenshots/notes_screen.png" alt="Notlar" width="200">
  <img src="screenshots/finance_screen.png" alt="Finans" width="200">
  <img src="screenshots/add_note.png" alt="Not Ekleme" width="200">
</div>

---

## 🚀 Kurulum

### Gereksinimler
- **Android**: 7.0 (API 24) veya üzeri
- **RAM**: Minimum 2GB
- **Depolama**: 50MB boş alan

### APK İndirme
1. [Releases](https://github.com/srcnbllc/Dayplanner/releases) sayfasından en son sürümü indirin
2. APK dosyasını Android cihazınıza yükleyin
3. "Bilinmeyen kaynaklardan yükleme" iznini verin
4. Uygulamayı başlatın

### Kaynak Koddan Derleme
```bash
# Repository'yi klonlayın
git clone https://github.com/srcnbllc/Dayplanner.git
cd Dayplanner

# Android Studio'da açın
# Gradle sync yapın
# Debug APK oluşturun
```

---

## 📖 Kullanım Kılavuzu

### Not Yönetimi
1. **Not Ekleme**: Ana ekranda "+" butonuna tıklayın
2. **Kategorize Etme**: Notları iş, kişisel, fikir olarak etiketleyin
3. **Şifreleme**: Önemli notları şifreleyin
4. **Pinleme**: Önemli notları üst sırada tutun
5. **Arama**: Üst çubukta arama yapın

### Finans Yönetimi
1. **Finans Sekmesi**: Alt menüden Finans'a gidin
2. **İşlem Ekleme**: "+" butonuna tıklayın
3. **Gelir/Gider**: İşlem türünü seçin
4. **Kategori**: Uygun kategoriyi belirleyin
5. **Filtreleme**: Tarih aralığı ve kategori filtrelerini kullanın

### Güvenlik Ayarları
1. **Ayarlar**: Menüden Ayarlar'a gidin
2. **Biyometrik**: Parmak izi/yüz tanımayı etkinleştirin
3. **Şifre**: Ana şifre belirleyin
4. **Otomatik Kilitleme**: Süre ayarlayın

---

## 🛠️ Teknik Detaylar

### Mimari
- **MVVM Pattern**: Model-View-ViewModel mimarisi
- **Room Database**: Yerel veri saklama
- **LiveData**: Reaktif veri akışı
- **Coroutines**: Asenkron işlemler
- **Navigation Component**: Fragment geçişleri

### Kullanılan Teknolojiler
- **Kotlin**: Ana programlama dili
- **Android Jetpack**: Modern Android geliştirme
- **Material Design 3**: UI tasarım sistemi
- **Room**: Veritabanı ORM
- **WorkManager**: Arka plan işlemleri
- **Biometric**: Biyometrik kimlik doğrulama

### Veritabanı Yapısı
```sql
-- Notlar tablosu
CREATE TABLE note_table (
    id INTEGER PRIMARY KEY,
    title TEXT,
    description TEXT,
    date TEXT,
    isPinned BOOLEAN,
    isLocked BOOLEAN,
    isEncrypted BOOLEAN,
    encryptedBlob BLOB,
    createdAt LONG
);

-- İşlemler tablosu
CREATE TABLE transactions (
    id INTEGER PRIMARY KEY,
    type TEXT, -- INCOME/EXPENSE
    subtype TEXT,
    title TEXT,
    amount REAL,
    currency TEXT,
    category TEXT,
    date LONG,
    meta TEXT -- JSON
);
```

---

## 📊 Sürüm Geçmişi

### v3.0 (Mevcut)
- ✅ Finans modülü eklendi
- ✅ Dashboard özet kartları
- ✅ Dinamik form sistemi
- ✅ Performans optimizasyonları
- ✅ Bug düzeltmeleri

### v2.0
- ✅ Pinleme sistemi
- ✅ Şifreleme özellikleri
- ✅ Çöp kutusu sistemi
- ✅ UI iyileştirmeleri

### v1.0
- ✅ Temel not yönetimi
- ✅ Kategori sistemi
- ✅ Arama özelliği
- ✅ Material Design

---

## 🤝 Katkıda Bulunma

1. Bu repository'yi fork edin
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add amazing feature'`)
4. Branch'inizi push edin (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

### Geliştirme Kuralları
- **Kod Stili**: Kotlin Coding Conventions
- **Commit Mesajları**: Conventional Commits
- **Test**: Unit testler yazın
- **Dokümantasyon**: Kod yorumları ekleyin

---

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.

---

## 📞 İletişim

- **Geliştirici**: [@srcnbllc](https://github.com/srcnbllc)
- **Proje Linki**: [https://github.com/srcnbllc/Dayplanner](https://github.com/srcnbllc/Dayplanner)
- **Sorun Bildirimi**: [Issues](https://github.com/srcnbllc/Dayplanner/issues)

---

## 🙏 Teşekkürler

- **Android Team**: Harika platform için
- **Kotlin Team**: Güçlü dil için
- **Material Design**: Tasarım sistemi için
- **Açık Kaynak Topluluğu**: İlham ve destek için

---

<div align="center">
  <p>⭐ Bu projeyi beğendiyseniz yıldız vermeyi unutmayın!</p>
  <p>Made with ❤️ by DayPlanner Team</p>
</div>

---

# 📱 DayPlanner - Personal Note and Finance Management App

<div align="center">
  <img src="app/src/main/res/drawable/ic_kite_star.xml" alt="DayPlanner Logo" width="100" height="100">
  
  **Modern, Secure and User-Friendly Note Management**
  
  [![Version](https://img.shields.io/badge/version-3.0-blue.svg)](https://github.com/srcnbllc/Dayplanner/releases/tag/v3.0)
  [![Android](https://img.shields.io/badge/Android-24%2B-green.svg)](https://developer.android.com/)
  [![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple.svg)](https://kotlinlang.org/)
  [![License](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)
</div>

---

## 🌟 Features

### 📝 Note Management
- **Smart Notes**: Categorized note system
- **Encryption**: Secure AES-256 encryption
- **Pinning**: Keep important notes at the top
- **Search**: Advanced search and filtering
- **Trash**: 30-day restore system

### 💰 Finance Management
- **Dashboard**: Income, expense and balance summary
- **Dynamic Forms**: Customized fields based on income/expense types
- **Filtering**: Date range and category-based filtering
- **Reporting**: Detailed financial analysis
- **Reminders**: Payment date notifications

### 🔐 Security
- **Biometric Authentication**: Fingerprint and face recognition
- **Encrypted Notes**: AES-256 encryption
- **Secure Storage**: Local encryption with Room Database

### 🎨 Modern Design
- **Kite Design**: Custom design system
- **Material Design 3**: Modern Android design language
- **Dark/Light Theme**: Automatic theme support
- **Responsive UI**: Compatible with all screen sizes

---

## 📱 Screenshots

<div align="center">
  <img src="screenshots/main_screen.png" alt="Main Screen" width="200">
  <img src="screenshots/notes_screen.png" alt="Notes" width="200">
  <img src="screenshots/finance_screen.png" alt="Finance" width="200">
  <img src="screenshots/add_note.png" alt="Add Note" width="200">
</div>

---

## 🚀 Installation

### Requirements
- **Android**: 7.0 (API 24) or higher
- **RAM**: Minimum 2GB
- **Storage**: 50MB free space

### APK Download
1. Download the latest version from [Releases](https://github.com/srcnbllc/Dayplanner/releases)
2. Install the APK file on your Android device
3. Allow "Install from unknown sources" permission
4. Launch the application

### Build from Source
```bash
# Clone the repository
git clone https://github.com/srcnbllc/Dayplanner.git
cd Dayplanner

# Open in Android Studio
# Perform Gradle sync
# Build debug APK
```

---

## 📖 User Guide

### Note Management
1. **Add Note**: Click the "+" button on the main screen
2. **Categorize**: Tag notes as work, personal, ideas
3. **Encrypt**: Encrypt important notes
4. **Pin**: Keep important notes at the top
5. **Search**: Use the search bar at the top

### Finance Management
1. **Finance Tab**: Go to Finance from the bottom menu
2. **Add Transaction**: Click the "+" button
3. **Income/Expense**: Select transaction type
4. **Category**: Choose appropriate category
5. **Filter**: Use date range and category filters

### Security Settings
1. **Settings**: Go to Settings from the menu
2. **Biometric**: Enable fingerprint/face recognition
3. **Password**: Set main password
4. **Auto Lock**: Set time duration

---

## 🛠️ Technical Details

### Architecture
- **MVVM Pattern**: Model-View-ViewModel architecture
- **Room Database**: Local data storage
- **LiveData**: Reactive data flow
- **Coroutines**: Asynchronous operations
- **Navigation Component**: Fragment transitions

### Technologies Used
- **Kotlin**: Main programming language
- **Android Jetpack**: Modern Android development
- **Material Design 3**: UI design system
- **Room**: Database ORM
- **WorkManager**: Background tasks
- **Biometric**: Biometric authentication

### Database Structure
```sql
-- Notes table
CREATE TABLE note_table (
    id INTEGER PRIMARY KEY,
    title TEXT,
    description TEXT,
    date TEXT,
    isPinned BOOLEAN,
    isLocked BOOLEAN,
    isEncrypted BOOLEAN,
    encryptedBlob BLOB,
    createdAt LONG
);

-- Transactions table
CREATE TABLE transactions (
    id INTEGER PRIMARY KEY,
    type TEXT, -- INCOME/EXPENSE
    subtype TEXT,
    title TEXT,
    amount REAL,
    currency TEXT,
    category TEXT,
    date LONG,
    meta TEXT -- JSON
);
```

---

## 📊 Version History

### v3.0 (Current)
- ✅ Finance module added
- ✅ Dashboard summary cards
- ✅ Dynamic form system
- ✅ Performance optimizations
- ✅ Bug fixes

### v2.0
- ✅ Pinning system
- ✅ Encryption features
- ✅ Trash system
- ✅ UI improvements

### v1.0
- ✅ Basic note management
- ✅ Category system
- ✅ Search feature
- ✅ Material Design

---

## 🤝 Contributing

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Rules
- **Code Style**: Kotlin Coding Conventions
- **Commit Messages**: Conventional Commits
- **Testing**: Write unit tests
- **Documentation**: Add code comments

---

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 📞 Contact

- **Developer**: [@srcnbllc](https://github.com/srcnbllc)
- **Project Link**: [https://github.com/srcnbllc/Dayplanner](https://github.com/srcnbllc/Dayplanner)
- **Issue Reporting**: [Issues](https://github.com/srcnbllc/Dayplanner/issues)

---

## 🙏 Acknowledgments

- **Android Team**: For the amazing platform
- **Kotlin Team**: For the powerful language
- **Material Design**: For the design system
- **Open Source Community**: For inspiration and support

---

<div align="center">
  <p>⭐ If you liked this project, don't forget to give it a star!</p>
  <p>Made with ❤️ by DayPlanner Team</p>
</div>
