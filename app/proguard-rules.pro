-keep class top.yukonga.fontWeightFix.MainHook {
    <init>();
}

-dontwarn androidx.annotation.NonNull
-dontwarn androidx.annotation.Nullable
-dontwarn androidx.annotation.RequiresApi

-keep class androidx.annotation.NonNull { *; }
-keep class androidx.annotation.Nullable { *; }
-keep class androidx.annotation.RequiresApi { *; }

-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations

