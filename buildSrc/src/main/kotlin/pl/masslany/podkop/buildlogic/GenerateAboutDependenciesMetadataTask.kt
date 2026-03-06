package pl.masslany.podkop.buildlogic

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerateAboutDependenciesMetadataTask : DefaultTask() {
    @get:Input
    abstract val pomFileMappings: MapProperty<String, String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val notices = pomFileMappings.get()
            .map { (notation, pomPath) -> AboutDependencyCoordinate(notation = notation, pomPath = pomPath) }
            .map { coordinate ->
                val pomFile = coordinate.pomPath
                    .takeIf(String::isNotBlank)
                    ?.let(::File)
                pomFile?.let { parsePomNotice(it, coordinate) } ?: AboutDependencyNotice(
                    displayName = coordinate.name,
                    artifact = coordinate.artifact,
                    licenseName = null,
                    licenseUrl = null,
                    projectUrl = null,
                )
            }
            .sortedBy { notice -> notice.displayName.lowercase() }

        val content = buildString {
            appendLine("package pl.masslany.podkop.features.about.generated")
            appendLine()
            appendLine("import pl.masslany.podkop.features.about.OpenSourceLibraryNotice")
            appendLine()
            appendLine("internal val GeneratedOpenSourceLibraries: List<OpenSourceLibraryNotice> = listOf(")
            notices.forEachIndexed { index, notice ->
                appendLine("    OpenSourceLibraryNotice(")
                appendLine("        name = ${notice.displayName.asKotlinLiteral()},")
                appendLine("        artifact = ${notice.artifact.asKotlinLiteral()},")
                appendLine("        licenseName = ${notice.licenseName.asKotlinLiteral()},")
                appendLine("        licenseUrl = ${notice.licenseUrl.asKotlinLiteral()},")
                appendLine("        projectUrl = ${notice.projectUrl.asKotlinLiteral()},")
                append("    )")
                if (index != notices.lastIndex) {
                    append(',')
                }
                appendLine()
            }
            appendLine(")")
        }

        val generatedFile = outputDirectory.file(
            "pl/masslany/podkop/features/about/generated/GeneratedOpenSourceLibraries.kt",
        ).get().asFile
        generatedFile.parentFile.mkdirs()
        generatedFile.writeText(content)
    }
}

private data class AboutDependencyCoordinate(
    val notation: String,
    val pomPath: String,
) {
    private val segments = notation.split(':', limit = 3)
    val group: String = segments[0]
    val name: String = segments[1]
    val version: String = segments[2]
    val artifact: String = "$group:$name"
}

private data class AboutDependencyNotice(
    val displayName: String,
    val artifact: String,
    val licenseName: String?,
    val licenseUrl: String?,
    val projectUrl: String?,
)

private fun parsePomNotice(
    pomFile: File,
    coordinate: AboutDependencyCoordinate,
): AboutDependencyNotice {
    val document = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(pomFile)
    val root = document.documentElement
    val licenses = root.getElementsByTagName("license")
        .elements()
        .mapNotNull { licenseElement ->
            val name = licenseElement.firstChildText("name")
            val url = licenseElement.firstChildText("url")
            if (name == null && url == null) {
                null
            } else {
                name to url
            }
        }

    return AboutDependencyNotice(
        displayName = root.firstChildText("name") ?: coordinate.name,
        artifact = coordinate.artifact,
        licenseName = licenses.mapNotNull { it.first }.distinct().joinToString().ifBlank { null },
        licenseUrl = licenses.firstNotNullOfOrNull { it.second },
        projectUrl = root.firstChildText("url"),
    )
}

private fun org.w3c.dom.NodeList.elements(): List<org.w3c.dom.Element> =
    (0 until length).mapNotNull { item(it) as? org.w3c.dom.Element }

private fun org.w3c.dom.Element.firstChildText(tagName: String): String? =
    getElementsByTagName(tagName)
        .item(0)
        ?.textContent
        ?.trim()
        ?.takeIf(String::isNotBlank)

private fun String.escapeKotlinString(): String = buildString(length) {
    for (character in this@escapeKotlinString) {
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(character)
        }
    }
}

private fun String?.asKotlinLiteral(): String = if (this == null) {
    "null"
} else {
    "\"${escapeKotlinString()}\""
}
