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

# 高德地图 SDK - 解决重复类冲突
# 保留 search 模块中的类，忽略 location 模块中的重复类
-keep class com.amap.apis.utils.core.api.AMapUtilCoreApi { *; }
-keep class com.amap.apis.utils.core.api.NetProxy { *; }
-dontwarn com.amap.apis.utils.core.api.**
