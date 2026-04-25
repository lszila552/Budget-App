# ─── Room ─────────────────────────────────────────────────────────────────────
# Keep entity and DAO classes so Room's generated code can access them at runtime.
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.migration.Migration { *; }
-keep class androidx.room.** { *; }

# ─── App data model and DB layer ──────────────────────────────────────────────
-keep class com.vrijgeld.data.model.** { *; }
-keep class com.vrijgeld.data.db.** { *; }

# ─── SQLCipher ────────────────────────────────────────────────────────────────
-keep class net.zetetic.database.** { *; }
-keep class net.zetetic.** { *; }

# ─── Hilt / Dagger ────────────────────────────────────────────────────────────
# Hilt ships its own consumer rules, but keep generated Hilt components just in case.
-keep class dagger.hilt.** { *; }
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# ─── Kotlin coroutines ────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ─── WorkManager ──────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ─── Glance AppWidget ─────────────────────────────────────────────────────────
-keep class androidx.glance.** { *; }

# ─── Vico charts ──────────────────────────────────────────────────────────────
-keep class com.patrykandpatrick.vico.** { *; }

# ─── Biometric ────────────────────────────────────────────────────────────────
-keep class androidx.biometric.** { *; }

# ─── Security Crypto (EncryptedSharedPreferences / DatabaseKeyManager) ─────────
-keep class androidx.security.crypto.** { *; }

# ─── Google Tink (transitive dep of security-crypto) ─────────────────────────
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**

# ─── Kotlin serialization ─────────────────────────────────────────────────────
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *; }

# ─── General Kotlin / reflection safety ──────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
