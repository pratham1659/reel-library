# ==============================================================================
# Security & ProGuard Optimization Rules for Reel Library
# ==============================================================================

# 1. Strip diagnostic logcat statements from release binaries
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# 2. Preserve Kotlinx Serialization reflection and companion serializers
-keepattributes *Annotation*,InnerClasses,Signature
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * implements kotlinx.serialization.internal.GeneratedSerializer {
    <fields>;
    <methods>;
}
# Targeted constructor preservation for reflection, serialization, and lifecycle instantiations
-keepclassmembers @kotlinx.serialization.Serializable class * {
    <init>(...);
}
-keepclassmembers @androidx.room.Entity class * {
    <init>(...);
}
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# 3. Room Database & SQLite FTS
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# 4. MediaPipe Tasks GenAI
-keep class com.google.mediapipe.tasks.genai.** { *; }
-dontwarn com.google.mediapipe.tasks.genai.**

# 5. OkHttp & Okio Network Stack
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# 6. AndroidX Security Crypto & MasterKey
-keep class androidx.security.crypto.** { *; }

# 7. AutoValue and Annotation Processing Shaded Classes
-dontwarn javax.lang.model.**
-dontwarn com.google.auto.value.**

