
import org.gradle.api.artifacts.ExternalModuleDependency
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask
import pl.masslany.podkop.buildlogic.GenerateAboutDependenciesMetadataTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinter)
    kotlin("plugin.serialization") version libs.versions.kotlinx.serialization
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

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies  {
            implementation(libs.ktor.client.android)
            implementation(libs.androidx.browser)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.compose.ui.tooling)
            implementation(libs.coil.svg)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.window.sizes)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.org.jetbrains.compose.material3.adaptive.adaptive)
            implementation(libs.adaptive.layout)
            implementation(libs.adaptive.navigation)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.lifecycle.viewmodel.navigation3)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.coil.compose)
            implementation(libs.coil.gif)
            implementation(libs.coil.network.ktor)
            implementation(libs.kotlinx.datetime)
            implementation(libs.multiplatform.markdown.renderer)
            implementation(libs.multiplatform.markdown.renderer.m3)
            implementation(libs.haze)

            implementation(projects.business)
        }

        iosMain.dependencies  {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }

    androidLibrary {
        namespace = "pl.masslany.podkop.composeapp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        androidResources.enable = true

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

kotlin.sourceSets.named("commonMain") {
    kotlin.srcDir(layout.buildDirectory.dir("generated/source/about/kotlin"))
}

val generateAboutDependenciesMetadata = tasks.register<GenerateAboutDependenciesMetadataTask>(
    "generateAboutDependenciesMetadata",
) {
    description = "Generates the About screen dependency metadata from declared project dependencies."
    group = "code generation"
    pomFileMappings.set(aboutPomFileMappings)
    outputDirectory.set(layout.buildDirectory.dir("generated/source/about/kotlin"))
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateAboutDependenciesMetadata)
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
