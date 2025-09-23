pluginManagement {
    repositories {
        google() // Android ve Google kütüphaneleri
        mavenCentral() // Kotlin ve diğer bağımlılıklar
        gradlePluginPortal() // Gradle pluginleri
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // JitPack repository for GitHub libraries (if needed)
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Dayplanner"
include(":app")
