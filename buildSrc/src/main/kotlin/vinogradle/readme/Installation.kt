package vinogradle.readme

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import vinogradle.*

/**
 * Finds "README.md" file in the project root and automatically adds installation instructions.
 **/
abstract class Installation : DefaultTask() {
    @Input
    @Optional
    var githubUrl: String? = null

    @Input
    var sectionTitle: String = "Install"

    @Input
    var mavenCentral: Boolean = false

    private fun mavenCentralMd() =
        mavenCentral.let {
            if (it) {
                project.toLibVer().toGradleInstallationMd()
                    .toSpoiler("Install from Maven Central with Gradle") + "\n\n" +
                    project.toLibVer().toMavenInstallationMd()
                        .toSpoiler("Install from Maven Central with Maven") + "\n\n"
            } else
                ""
        }

    private fun githubMd() =                 githubUrl.let {
        if (it != null)
            project.toLibVer().toGithubInstallationMd(it)
                .toSpoiler("Install latest from GitHub with Gradle/Kotlin") + "\n\n"
        else
            ""

    }


    @TaskAction
    fun update() {
        val instructionsMd = mavenCentralMd() + githubMd()
        project.rootDir.resolve("README.md").apply {
            writeText(readText().replaceSectionInMd(sectionTitle, instructionsMd))
        }
    }
}