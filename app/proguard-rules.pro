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

# 1. Keep all data models in your package
-keep class com.example.localgrub.data.model.** { *; }

# 2. Keep all generated hidden classes (like $serializer) for those models
-keep class com.example.localgrub.data.model.**$* { *; }

# 3. Keep essential attributes that Kotlinx Serialization needs to read JSON
-keepattributes *Annotation*, InnerClasses, Signature