# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in 'proguard-android-optimize.txt' which is shipped with the Android SDK.

# ----------------------------------------------------------------------------
# Flutter / Dart (If applicable - likely not for this native app but good safety)
# ----------------------------------------------------------------------------
-keep class io.flutter.app.** { *; }
-keep class io.flutter.plugin.**  { *; }
-keep class io.flutter.util.**  { *; }
-keep class io.flutter.view.**  { *; }
-keep class io.flutter.**  { *; }
-keep class io.flutter.plugins.**  { *; }

# ----------------------------------------------------------------------------
# Android & Jetpack Compose
# ----------------------------------------------------------------------------
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }

# Keep all @Composable functions
-keepnames class * {
    @androidx.compose.runtime.Composable *;
}

# ----------------------------------------------------------------------------
# Room Persistence Library
# ----------------------------------------------------------------------------
-keep class androidx.room.RoomDatabase { *; }
-keep class androidx.room.pooling.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase$Builder { *; }

# Keep Entities and DAOs
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Allow R8 to strip methods if unused, but keep the class names if reflected
-keepclassmembers class * {
    @androidx.room.PrimaryKey *;
    @androidx.room.ColumnInfo *;
    @androidx.room.Embedded *;
    @androidx.room.Relation *;
    @androidx.room.ForeignKey *;
}

# ----------------------------------------------------------------------------
# Hilt / Dagger
# ----------------------------------------------------------------------------
-keep class com.google.dagger.** { *; }
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class javax.annotation.** { *; }
-keep class * extends dagger.internal.Factory

# Keep Hilt generated components
-keep public class * extends dagger.hilt.internal.aggregatedroot.AggregatedRoot
-keep public class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keep class dagger.hilt.android.internal.earlyentrypoint.AggregatedEarlyEntryPoint
-keep class dagger.hilt.android.internal.managers.ApplicationComponentManager
-keep class dagger.hilt.android.internal.managers.ActivityComponentManager

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }

# ----------------------------------------------------------------------------
# Kotlin Coroutines
# ----------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.Dumpable {}

# ----------------------------------------------------------------------------
# General Safety
# ----------------------------------------------------------------------------
# Serialized classes (Gson/Moshi/Kotlinx.serialization) - just in case
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <methods>;
}

# Keep our data classes just to be safe if reflected by libraries
-keep class com.wlaz.brainfood.data.** { *; }

# Enum members
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ----------------------------------------------------------------------------
# Firebase
# ----------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Auth
-keep class com.firebase.ui.auth.** { *; }

# Firestore
-keep class com.google.cloud.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName *;
}

# Google Credential Manager
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-dontwarn androidx.credentials.**
