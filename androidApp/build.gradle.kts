
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
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
        applicationId = "pl.masslany.podkop"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 16
        versionName = "2.6.0"

        buildConfigField("String", "WYKOP_KEY", "\"${apikeyProperties.getProperty("WYKOP_KEY")}\"")
        buildConfigField("String", "WYKOP_SECRET", "\"${apikeyProperties.getProperty("WYKOP_SECRET")}\"")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(projects.business)
    implementation(projects.common)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.android)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)

    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.leakcanary.android)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotlinx.datetime)
}
