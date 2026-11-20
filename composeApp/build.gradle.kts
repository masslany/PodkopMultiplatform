
import org.gradle.api.artifacts.ExternalModuleDependency
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask
import pl.masslany.podkop.buildlogic.GenerateAboutDependenciesMetadataTask

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlinSerialization)
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

android {
    namespace = "pl.masslany.podkop.composeapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
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

    implementation(projects.business)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotlinx.coroutines.test)
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
