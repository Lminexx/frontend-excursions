# Add project specific ProGuard rules here.
# For more details, see:
#   http://developer.android.com/guide/developing/tools/proguard.html

-keepattributes Signature
-keepattributes *Annotation*

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep class com.example.projectexcursions.models.** { *; }

-keep class * extends com.google.gson.reflect.TypeToken {
    <init>(...);
}

-keepclassmembers class * {
    public <init>();
}

-keep class com.yandex.** { *; }

-keep class com.google.gson.** { *; }

-dontwarn aQute.bnd.annotation.spi.ServiceProvider
-dontwarn javax.xml.stream.XMLEventFactory
-dontwarn javax.xml.stream.XMLInputFactory
-dontwarn javax.xml.stream.XMLOutputFactory
-dontwarn javax.xml.stream.XMLResolver
-dontwarn javax.xml.stream.util.XMLEventAllocator
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn java.beans.ConstructorProperties
-dontwarn java.beans.Transient