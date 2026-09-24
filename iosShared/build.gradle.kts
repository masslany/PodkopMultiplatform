plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "PodkopShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.business)
            implementation(projects.common)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
