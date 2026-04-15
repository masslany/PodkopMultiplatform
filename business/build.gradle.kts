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

        namespace = "pl.masslany.podkop.business"
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
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.serialization.kotlinx.json)

            api(projects.common)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

kotlin.sourceSets.named("androidHostTest") {
    dependencies {
        implementation(libs.kotlin.testJunit)
        implementation(libs.kotlinx.coroutines.test)
    }
}
