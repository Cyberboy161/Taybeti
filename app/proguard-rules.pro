# Strip all logging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Keep Room entities
-keep class com.taybeti.app.data.entities.** { *; }

# Keep Argon2
-keep class de.mkammerer.argon2.** { *; }

# Strip debug/sensitive class names
-allowaccessmodification
-repackageclasses
