
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

val apikeyPropertiesFile: File = rootProject.file("apikeys.properties")
val apikeyProperties =
    Properties().apply {
        load(FileInputStream(apikeyPropertiesFile))
    }

// The Android release workflow passes the release version; every other build is a dev build, so its
// crashes are easy to tell apart (and filter out) in Crashlytics.
val podkopVersionName: String = providers.gradleProperty("podkop.versionName").getOrElse("0.0.0-dev")
val podkopVersionCode: Int = providers.gradleProperty("podkop.versionCode").map(String::toInt).getOrElse(1)

android {
    namespace = "pl.masslany.podkop"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "pl.masslany.podkop"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = podkopVersionCode
        versionName = podkopVersionName

        testInstrumentationRunner = "pl.masslany.podkop.test.PodkopTestRunner"
        // With the orchestrator, every test gets a fresh app process with its data cleared.
        testInstrumentationRunnerArguments["clearPackageData"] = "true"

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
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        managedDevices {
            // Run integration tests on it with `./gradlew :androidApp:pixel6Api34DebugAndroidTest`.
            localDevices {
                create("pixel6Api34") {
                    device = "Pixel 6"
                    apiLevel = 34
                    systemImageSource = "aosp-atd"
                }
            }
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
    implementation(libs.timber)

    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.leakcanary.android)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.okhttp)
    androidTestImplementation(libs.mockwebserver3)
    androidTestUtil(libs.androidx.test.orchestrator)
    androidTestUtil(libs.androidx.test.services)
}
