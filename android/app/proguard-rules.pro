# Proguard rules for SCAMSHIELD
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
-keep class com.scamshield.app.data.model.** { *; }
