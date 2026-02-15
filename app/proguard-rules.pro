# Retrofit
-keepattributes Signature
-keepattributes Annotation
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }

# Gson
-keep class com.raushan.upscagent.data.model.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
