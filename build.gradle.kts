plugins {
    // 1. Android Application Plugin
    id("com.android.application") version "8.2.2" apply false

    // 2. Kotlin Plugin (Update this to 2.0.0 to fix the "binary version" errors)
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false

    // 3. Google Services Plugin (For Firebase)
    id("com.google.gms.google-services") version "4.4.0" apply false

    // 4. Compose Compiler Plugin (Required for Kotlin 2.0.0+)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}