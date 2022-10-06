package vinogradle.readme

import org.gradle.api.*


data class LibVer(val group: String, val artifact: String, val version: String)

fun Project.toLibVer() =
    LibVer(
        group = this.group.toString(),
        artifact = this.name,
        version = """[0-9\.]+""".toRegex()
            .find(this.version.toString())!!
            .groupValues.single()
    )

fun LibVer.toGithubInstallationMd(
    repoUrl: String,
    branch: String = "staging",
) =
    """
        #### settings.gradle.kts
        
        ```kotlin
        sourceControl {
            gitRepository(java.net.URI("$repoUrl.git")) {
                producesModule("${this.group}:${this.artifact}")
            }
        }
        ```
        
        #### build.gradle.kts
        
        ```kotlin
        dependencies {
            implementation("${this.group}:${this.artifact}") {
                version { branch = "$branch" }
            }
        }
        ```
    """.trimIndent()

fun String.toSpoiler(summary: String) =
    "<details><summary>$summary</summary>\n\n$this\n\n</details>"

fun LibVer.toGradleInstallationMd() =
    """
        ### build.gradle.kts (Gradle/Kotlin)
        
        ```kotlin
        repositories {
            mavenCentral()
        }                
        
        dependencies {
            implementation("$group:$artifact:$version")
        }    
        ```
        
        or
        
        ### build.gradle (Gradle/Groovy)
        
        ```groovy
        repositories {
            mavenCentral()
        }                
        
        dependencies {
            implementation "$group:$artifact:$version"
        }
        ```
    """.trimIndent()

fun LibVer.toMavenInstallationMd() =
    """
    ## Maven
    
    ```xml    
    <dependencies>
        <dependency>
            <groupId>$group</groupId>
            <artifactId>$artifact</artifactId>
            <version>$version</version>
        </dependency>
    </dependencies>
    ```
    """.trimIndent()


/** Replaces text between "# Title" and "# Next Title". */
public fun String.replaceSectionInMd(
    sectionTitle: String,
    newSectionText: String,
): String {
    var changesMade = 0

    val result = "([\n\r]#\\s+$sectionTitle\\s*[\n\r]).*?([\n\r]#\\s)"
        .toRegex(RegexOption.DOT_MATCHES_ALL)
        .replace(this) {
            changesMade = 1
            it.groups[1]!!.value + newSectionText + it.groups[2]!!.value
        }

    require(changesMade==1) { "changesMade=$changesMade" }

    return result
}