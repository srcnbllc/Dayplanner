// Proje düzeyindeki build.gradle.kts dosyasının başı
buildscript {
    repositories {
        google()  // Google Maven Repository
        mavenCentral()  // Maven Central Repository
    }
    dependencies {
        // Android Gradle Plugin için doğru sürüm
        classpath("com.android.tools.build:gradle:8.6.0")

        // Kotlin Gradle Plugin için doğru sürüm
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
    }
}

task("clean", Delete::class) {
    delete(layout.buildDirectory.asFile.get())  // build klasörünü temizler
}
