import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.kover)
    kotlin("plugin.serialization") version libs.versions.kotlinx.serialization
}

kotlin {
    androidLibrary {
        withHostTestBuilder {}.configure {
            isIncludeAndroidResources = true
        }

        namespace = "pl.masslany.podkop.common"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        androidResources.enable = true

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

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
            implementation(libs.ktor.client.mock)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            api(libs.ktor.client.okhttp)
            implementation(libs.timber)
        }
        
        iosMain.dependencies {
            api(libs.ktor.client.darwin)
        }
    }
}

kotlin.sourceSets.named("androidHostTest") {
    dependencies {
        implementation(libs.kotlin.testJunit)
        implementation(libs.kotlinx.coroutines.test)
    }
}
