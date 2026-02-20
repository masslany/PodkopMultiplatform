import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Podkop"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.koin.core)
            api(libs.ktor.client.core)
            api(libs.ktor.client.logging)
            api(libs.ktor.client.auth)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.serialization.kotlinx.json)
            api(libs.androidx.datastore.preferences)

        }
        commonTest.dependencies {
            api(libs.kotlin.test)
        }

        androidMain.dependencies {
            api(libs.ktor.client.okhttp)
            implementation(libs.timber)
        }
        
        iosMain.dependencies {
            api(libs.ktor.client.darwin)
        }
    }

    androidLibrary {
        namespace = "pl.masslany.podkop.common"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        androidResources.enable = true

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

    }
}
