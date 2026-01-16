rootProject.name = "Podkop"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

fun isDirRelevant(dir: File): Boolean {
    val irrelevantDirs = listOf(
        ".git",
        ".gradle",
        ".idea",
        "buildSrc",
        "gradle",
        "build",
        "out"
    )
    return dir.name !in irrelevantDirs
}

fun discoverRecursive(root: File) {
    root.listFiles()?.forEach { dir ->
        if (isDirRelevant(dir)) {
            if (File(dir, "build.gradle").exists() ||  File(dir, "build.gradle.kts").exists()) {
                val project = File(root.path, dir.name)
                val projectPath = project.path
                    .substringAfter(rootDir.path)
                    .replace("/", ":")
                    .replace("\\", ":")
                settings.include(projectPath)
            } else {
                discoverRecursive(dir)
            }
        }
    }
}

discoverRecursive(rootDir)