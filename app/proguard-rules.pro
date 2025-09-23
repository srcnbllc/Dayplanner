# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ML Kit temporarily disabled for 16KB compatibility
# -keep class com.google.mlkit.** { *; }
# -keep class com.google.android.gms.** { *; }

# Keep Room classes
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Keep WorkManager classes
-keep class androidx.work.** { *; }

# Keep Biometric classes
-keep class androidx.biometric.** { *; }

# Keep Security Crypto classes
-keep class androidx.security.** { *; }

# 16 KB page size compatibility
-keep class **.R
-keep class **.R$*
-keep class **.BuildConfig
-keep class **.Manifest$*