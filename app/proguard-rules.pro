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

-keepclassmembers class com.medtroniclabs.opensource.data.** {*;}
-keepclassmembers class com.medtroniclabs.opensource.formgeneration.** {*;}

-keepattributes InnerClasses -keep class **.R -keep class **.R$* { <fields>; }

# GSON
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.examples.android.model.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

### Crashlytics
# In order to provide the most meaningful crash reports
-keepattributes SourceFile,LineNumberTable
# If you're using custom Eception
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

### Crash report
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable


# sqlite
-keep class org.sqlite.** { *; }
-keep class org.sqlite.database.** { *; }

# sqlcipher
-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

# Flexbox layout
-keepnames public class com.google.android.flexbox.FlexboxLayoutManager

# lottie
# -dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** {*;}

# Materialchipview
-keep public class com.robertlevonyan.views.** { public *;}
-keep public class com.robertlevonyan.chip.** { public *;}

-keepclassmembers class com.medtroniclabs.opensource.db.tables.** {*;}

-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type