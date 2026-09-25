import pl.masslany.podkop.buildlogic.GenerateAboutDependenciesMetadataTask

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

/*
 * About-screen notices for the libraries actually linked into PodkopShared (iosArm64 klibs),
 * resolved at execution time; Android keeps its own list in composeApp.
 */
val generateIosAboutDependenciesMetadata = tasks.register<GenerateAboutDependenciesMetadataTask>(
    "generateIosAboutDependenciesMetadata",
) {
    description = "Generates open source notices for the dependencies linked into the iOS framework."
    group = "code generation"
    packageName.set("pl.masslany.podkop.ios.generated")
    noticeType.set("pl.masslany.podkop.ios.IOSLibraryNotice")
    outputDirectory.set(layout.buildDirectory.dir("generated/source/about/kotlin"))
    val klibs = configurations.named("iosArm64CompileKlibraries")
    pomFileMappings.set(
        klibs.map { configuration ->
            configuration.incoming.resolutionResult.allComponents
                .filter { it.id is org.gradle.api.artifacts.component.ModuleComponentIdentifier }
                .mapNotNull { it.moduleVersion }
                .filter { it.group.isNotBlank() && !it.name.endsWith("-iosarm64") }
                .map { "${it.group}:${it.name}:${it.version}" }
                .distinctBy { it.substringBeforeLast(':') }
                .associateWith { notation ->
                    runCatching {
                        configurations.detachedConfiguration(project.dependencies.create("$notation@pom"))
                            .apply { isTransitive = false }
                            .singleFile.absolutePath
                    }.getOrDefault("")
                }
        },
    )
}

kotlin {
    sourceSets.iosMain {
        kotlin.srcDir(layout.buildDirectory.dir("generated/source/about/kotlin"))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateIosAboutDependenciesMetadata)
}
