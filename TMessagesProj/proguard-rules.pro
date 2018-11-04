-keep public class com.google.android.gms.* { public *; }
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}
-keep class io.bettergram.** { *; }
-keep class com.google.android.exoplayer2.** { *; }
-keep class com.coremedia.** { *; }
-keep class com.googlecode.mp4parser.** { *; }
-dontwarn com.coremedia.**
-dontwarn io.bettergram.**
-dontwarn com.google.android.exoplayer2.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.common.cache.**
-dontwarn com.google.common.primitives.**
-dontwarn com.googlecode.mp4parser.**
# Use -keep to explicitly keep any other classes shrinking would remove
-dontoptimize
-dontobfuscate
-dontwarn sun.misc.Unsafe
-dontwarn javax.annotation.**
-dontwarn org.jaxen.**
-dontwarn org.conscrypt.**
-dontwarn javax.xml.stream.**
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform