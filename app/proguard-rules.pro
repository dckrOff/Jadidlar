# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep data classes
-keep class uz.dckroff.jadidlar.data.models.** { *; }
-keepclassmembers class uz.dckroff.jadidlar.data.models.** { *; }

# Firebase
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# PDF Viewer
-keep class com.rajat.pdfviewer.** { *; }
-dontwarn com.rajat.pdfviewer.**

# ViewBinding
-keep class * implements androidx.viewbinding.ViewBinding {
    public static * inflate(android.view.LayoutInflater);
    public static * inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
    public static * bind(android.view.View);
}

# Navigation Component
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.fragment.app.Fragment{}
