# --- Mac Remote Proguard / R8 Optimization Rules ---

# Preserve Kotlin Reflection and Coroutines
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-dontwarn kotlin.**

# Jetpack Compose Rules
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# OkHttp & Okio Rules (WebSocket)
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*

# Gson Rules (Data Models)
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# Mac Remote Network & Model Classes
-keep class com.ibrahimtemur.macremote.network.** { *; }
-keep class com.ibrahimtemur.macremote.DiscoveryManager { *; }

# Line Numbers for Stacktraces in Google Play Console
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
