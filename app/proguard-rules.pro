# Keep Room generated implementations discoverable at runtime.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class com.jhonmaxdata.authenticator.data.local.** { *; }