
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
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
    buildTypes {
        release {
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

    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.android)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.activity.compose)
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-XXLanguage:+ExplicitBackingFields")
        }
    }
}
