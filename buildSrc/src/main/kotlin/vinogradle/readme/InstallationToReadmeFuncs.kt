//package convention.readme
package vinogradle

import org.gradle.api.*



//import org.gradle.api.Project

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

//         <details>
//          <summary>Latest (Kotlin Gradle)</summary>
//        </details>
//
//
//

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

//fun updateReadmeWithInstallationInstructions(readmeFile: File, githubUrl: String) {
//    val instructionsMd =
//        project.toLibVer().toGradleInstallationMd()
//            .toSpoiler("Install from Maven Central with Gradle") + "\n\n" +
//            project.toLibVer().toMavenInstallationMd()
//                .toSpoiler("Install from Maven Central with Maven") + "\n\n" +
//            project.toLibVer().toGithubInstallationMd(githubUrl)
//                .toSpoiler("Install latest from GitHub with Gradle/Kotlin") + "\n\n"
//
//    readmeFile.writeText(
//        readmeFile.readText().replaceSectionInMd("Install", instructionsMd)
//    )
//}

public object Readme {
    fun hello() {
        println("hello")
    }
}

fun hello2() = "hi!"





// Create a task using the task type
//tasks.register<GreetingTask>("hello")





//import org.gradle.api.Plugin
//    import org.gradle.api.Project
//    import org.gradle.kotlin.dsl.create

//open class HelloExtension(
//    var greeting: String = "Hello",
//    var name: String = "buddy"
//)
//
//class HelloPlugin : Plugin<Project> {
//    override fun apply(project: Project): Unit = project.run {
//        val hello = project.extensions.create<HelloExtension>("HelloExtension")
//        project.extensions.add("hello", hello)
//        tasks.register("hello") {
//            doLast {
//                println("${hello.greeting.capitalize()}, ${hello.name.capitalize()}!")
//            }
//        }
//    }
//}