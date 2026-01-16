
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "pl.masslany.podkop"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Pull in your Multiplatform UI and logic
    implementation(project(":composeApp"))

    // Add Android-specific runner dependencies
    implementation(libs.androidx.activity.compose)
}
