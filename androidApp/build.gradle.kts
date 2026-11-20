import java.io.FileInputStream
import java.util.Properties
import org.gradle.api.artifacts.ExternalModuleDependency
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask
import pl.masslany.podkop.buildlogic.GenerateAboutDependenciesMetadataTask

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.kover)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

private data class AboutDependencyCoordinate(
    val group: String,
    val name: String,
    val version: String,
) {
    val artifact: String = "$group:$name"
    val notation: String = "$group:$name:$version"
}

private val aboutConfigurationNamePattern = Regex("^[A-Za-z0-9]+Main(Api|Implementation)(DependenciesMetadata)?$")

private fun isAboutConfigurationName(name: String): Boolean =
    name == "api" || name == "implementation" || aboutConfigurationNamePattern.matches(name)

private fun ExternalModuleDependency.versionOrNull(): String? =
    version?.takeIf(String::isNotBlank)
        ?: versionConstraint.requiredVersion.takeIf(String::isNotBlank)
        ?: versionConstraint.preferredVersion.takeIf(String::isNotBlank)

private val aboutDependencyCoordinates = rootProject.allprojects
    .asSequence()
    .flatMap { candidateProject ->
        candidateProject.configurations
            .asSequence()
            .filter { configuration -> isAboutConfigurationName(configuration.name) }
            .flatMap { configuration ->
                configuration.dependencies
                    .withType(ExternalModuleDependency::class.java)
                    .asSequence()
            }
    }
    .mapNotNull { dependency ->
        val group = dependency.group?.takeIf(String::isNotBlank) ?: return@mapNotNull null
        val version = dependency.versionOrNull() ?: return@mapNotNull null
        AboutDependencyCoordinate(
            group = group,
            name = dependency.name,
            version = version,
        )
    }
    .distinctBy(AboutDependencyCoordinate::artifact)
    .sortedBy(AboutDependencyCoordinate::artifact)
    .toList()

private val aboutPomFileMappings = aboutDependencyCoordinates.associate { coordinate ->
    val pomFilePath = runCatching {
        configurations.detachedConfiguration(
            dependencies.create("${coordinate.notation}@pom"),
        ).apply {
            isTransitive = false
        }.singleFile.absolutePath
    }.getOrDefault("")

    coordinate.notation to pomFilePath
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
    compileSdk {
        version = release(libs.versions.android.compileSdk.get().toInt()) {
            minorApiLevel = libs.versions.android.compileSdkMinor.get().toInt()
        }
    }
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
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
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

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(projects.business)
    implementation(projects.common)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    implementation(libs.koin.android)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.timber)

    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.sizes)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigationevent.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    // Coil's Ktor fetcher uses whichever Ktor engine is on the classpath: keep that OkHttp (from
    // :common). Since Ktor 3.5.1 the Android engine closes a cancelled image download from the
    // main thread, which HttpURLConnection rejects with a crash.
    implementation(libs.coil.network.ktor)
    implementation(libs.kotlinx.datetime)
    implementation(libs.multiplatform.markdown.renderer)
    implementation(libs.multiplatform.markdown.renderer.m3)
    implementation(libs.haze)
    implementation(libs.haze.blur)


    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotlinx.coroutines.test)

    debugImplementation(libs.leakcanary.android)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.okhttp)
    androidTestImplementation(libs.mockwebserver3)
    androidTestUtil(libs.androidx.test.orchestrator)
    androidTestUtil(libs.androidx.test.services)
}

val generateAboutDependenciesMetadata = tasks.register<GenerateAboutDependenciesMetadataTask>(
    "generateAboutDependenciesMetadata",
) {
    description = "Generates the About screen dependency metadata from declared project dependencies."
    group = "code generation"
    pomFileMappings.set(aboutPomFileMappings)
    outputDirectory.set(layout.buildDirectory.dir("generated/source/about/kotlin"))
}

androidComponents {
    onVariants { variant ->
        variant.sources.kotlin?.addGeneratedSourceDirectory(
            generateAboutDependenciesMetadata,
            GenerateAboutDependenciesMetadataTask::outputDirectory,
        )
    }
}

afterEvaluate {
    tasks.withType<LintTask>().configureEach {
        exclude("**/build/**")
        exclude("**/generated/**")
    }

    tasks.withType<FormatTask>().configureEach {
        exclude("**/build/**")
        exclude("**/generated/**")
    }
}
