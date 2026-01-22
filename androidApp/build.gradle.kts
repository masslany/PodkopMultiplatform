
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

val apikeyPropertiesFile: File = rootProject.file("apikeys.properties")
val apikeyProperties =
    Properties().apply {
        load(FileInputStream(apikeyPropertiesFile))
    }

android {
    namespace = "pl.masslany.podkop"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        buildConfigField("String", "WYKOP_KEY", "\"${apikeyProperties.getProperty("WYKOP_KEY")}\"")
        buildConfigField("String", "WYKOP_SECRET", "\"${apikeyProperties.getProperty("WYKOP_SECRET")}\"")

    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(projects.business)
    implementation(projects.common)

    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.android)
    implementation(libs.androidx.core.splashscreen)

    // Add Android-specific runner dependencies
    implementation(libs.androidx.activity.compose)
}
