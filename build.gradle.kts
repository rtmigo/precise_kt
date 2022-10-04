plugins {
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.dokka") version "1.7.10"
    id("io.codearte.nexus-staging") version "0.30.0"

    id("maven-publish") // maven
    id("signing") // maven

    id("java-library")
    java
}


group = "io.github.rtmigo"
version = "0.0.0+9"

tasks.register("pkgver") {
    doLast {
        println(project.version.toString())
    }
}

java {
    withSourcesJar()  // для Maven Central
}

//val sonatypeUsername by lazy { System.getenv("SONATYPE_USERNAME")!! } // можно токен
//val sonatypePassword by lazy { System.getenv("SONATYPE_PASSWORD")!! } // можно токен

/**
 * В этом объекте объединены реквизиты Sonatype и GPG.
 *
 * Потому что подписывать пакеты GPG мы будем только при публикации в Sonatype,
 * но не GitHub Packages.
 **/
data class SonatypeCredentials(
    /** Имя пользователя Sonatype, либо токен username */
    val username: String = System.getenv("SONATYPE_USERNAME")!!,
    /** Пароль Sonatype, либо токен password */
    val password: String = System.getenv("SONATYPE_PASSWORD")!!,
    /** ASCII armored key */
    val gpgPrivateKey: String = System.getenv("MAVEN_GPG_KEY")!!,
    /** Пароль для дешифровки из [gpgPrivateKey] */
    val gpgPassword: String = System.getenv("MAVEN_GPG_PASSWORD")!!,
)

//data class GithubConfig(
//    val username: String = System.getenv("GITHUB_USERNAME"),
//    val password: String  = System.getenv("GITHUB_PKGPUB_TOKEN")
//)

fun configurePublishing(
    ownerSlashRepo: String,
    licenseKind: String,
    projectName: String,
    descriptionText: String,
    sonatype: SonatypeCredentials? = null,
    githubToken: String? = null,
) {
    if (sonatype == null && githubToken == null) return

    //require(sonatype!=null || githubToken!=null)

    publishing {
        //val ownerSlashRepo = "rtmigo/precise_kt"

        val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
        val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
            dependsOn(dokkaHtml)
            archiveClassifier.set("javadoc")
            from(dokkaHtml.outputDirectory)
        }

        //val projectName = "precise" // наверно как-то можно получить из мета-данных
        //val licenseKind = "MIT License"
        //val descrText = "Kotlin/JVM compensated summation of Double sequences " +
        //   "to calculate sum, mean, standard deviation "
        //val publishToGitHub = false
        //val publishToSonatype = true

        //val sonatypeUsername by lazy {}

        repositories {
            if (githubToken != null) maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/$ownerSlashRepo")

                credentials {
                    username = ownerSlashRepo.substringBefore("/")
//                            project.findProperty("gpr.user") as String?
//                                ?: System.getenv("GITHUB_USERNAME")
//                                    ?: ownerSlashRepo.substringBefore("/")
                    password = githubToken
//                            project.findProperty("gpr.key") as String?
//                          ?: System.getenv("GITHUB_PACKAGE_PUBLISHING_TOKEN")
                }
            }

            if (sonatype != null) maven {
                val releasesUrl =
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsUrl =
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = (if (version.toString().endsWith("SNAPSHOT"))
                    snapshotsUrl
                else
                    releasesUrl)
                credentials {
                    username = sonatype.username // можно токен
                    password = sonatype.password // можно токен
                }
            }
        }

        publications {

            register<MavenPublication>("maven") {
                from(components["java"])
                artifact(javadocJar)

                pom {
                    name.set(projectName)
                    url.set("https://github.com/$ownerSlashRepo")
                    description.set(descriptionText)

                    developers {
                        developer {
                            id.set("rtmigo")
                            name.set("Artsiom iG")
                            email.set("ortemeo@gmail.com")
                        }
                    }

                    licenses {
                        license {
                            name.set(licenseKind)
                            url.set("https://github.com/$ownerSlashRepo/blob/-/LICENSE")
                        }
                    }

                    scm {
                        connection.set("scm:https://github.com/$ownerSlashRepo.git")
                        developerConnection.set("scm:git@github.com:$ownerSlashRepo.git")
                        url.set("https://github.com/$ownerSlashRepo")
                    }
                }
            }
        }
    }

    if (sonatype != null) {
        nexusStaging {
            // конфигурируем плагин id("io.codearte.nexus-staging").
            // Задачу можно будет запустить так: ./gradlew closeAndReleaseRepository
            serverUrl = "https://s01.oss.sonatype.org/service/local/"
            username = sonatype.username
            password = sonatype.password
        }

        signing {
            // Using the following setup, you can pass the secret key (in ascii-armored format) and the
            // password using the ORG_GRADLE_PROJECT_signingKey and ORG_GRADLE_PROJECT_signingPassword
            // environment variables, respectively (https://bit.ly/3CuTCdD)
            //val signingKey: String? by project
            //val signingPassword: String? by project

            //println("Signing with key ${signingKey}")
            //println("Signing with pwd ${signingPassword}")

            useInMemoryPgpKeys(sonatype.gpgPrivateKey, sonatype.gpgPassword)
            sign(publishing.publications["maven"])
        }
    }
}

configurePublishing(
    ownerSlashRepo = "rtmigo/precise_kt",
    projectName = "precise",
    licenseKind = "MIT License",
    descriptionText = "Kotlin/JVM compensated summation of Double sequences " + "to calculate sum, mean, standard deviation ")



repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("io.kotest:kotest-assertions-core:5.4.2")
}

kotlin {
    sourceSets {
        val main by getting
        val test by getting
    }
}

tasks.test {
    useJUnitPlatform()
}


tasks.register("updateReadmeVersion") {
    doFirst {
        // найдем что-то вроде "io.github.rtmigo:dec:0.0.1"
        // и поменяем на актуальную версию
        val readmeFile = project.rootDir.resolve("README.md")
        val prefixToFind = "io.github.rtmigo:precise:"
        val regex = """(?<=${Regex.escape(prefixToFind)})[0-9\.+]+""".toRegex()
        val oldText = readmeFile.readText()
        val newText = regex.replace(oldText, project.version.toString())
        if (newText != oldText) readmeFile.writeText(newText)
    }
}

data class LibVer(val group: String, val artifact: String, val version: String)

fun Project.toLibVer() =
    LibVer(
        group = this.group.toString(),
        artifact = this.name,
        version = """[0-9\.]+""".toRegex()
            .find(this.version.toString())!!
            .groupValues.single()
    )

fun LibVer.toGithubInstallationMd(repoUrl: String,
                                  branch: String = "staging") =
    """
        ## Install latest from GitHub with Gradle (Kotlin)
        
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
        ## Gradle (Kotlin)
        
        ```kotlin
        repositories {
            mavenCentral()
        }                
        
        dependencies {
            implementation("$group:$artifact:$version")
        }    
        ```
        
        ## Gradle (Groovy)
        
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


fun String.replaceSectionInMd(
    sectionTitle: String,
    newSectionText: String) =// "#".toRegex().replace(this, newSectionText)
    "([\n\r]#\\s+$sectionTitle\\s*[\n\r]).*?([\n\r]#\\s)".toRegex(RegexOption.DOT_MATCHES_ALL)
        .replace(this) { it.groups[1]!!.value+newSectionText+ it.groups[2]!!.value}



tasks.register("genInstallation") {
    doFirst {
//        val pkgGroup = project.group.toString()
//        val pkgLib = project.name
//        val pkgVer = """[0-9\.]+""".toRegex()
//            .find(project.version.toString())!!
//            .groupValues.single()



        val code =

                project.toLibVer().toGradleInstallationMd().toSpoiler("with Gradle from Maven Central")+"\n\n"+
                project.toLibVer().toMavenInstallationMd().toSpoiler("with Maven from Maven Central")+"\n\n"+
                project.toLibVer().toGithubInstallationMd("https://github.com/rtmigo/precise_kt").toSpoiler("with Gradle from GitHub")+"\n\n"

        val readme = project.projectDir.resolve("README.md")
        val readmeNew = project.projectDir.resolve("README.new.md")
        readmeNew.writeText(
        readme.readText().replaceSectionInMd("Install", code)
        )


//        project.projectDir.resolve("doc").mkdirs()
//        project.projectDir.resolve("doc/install.md").writeText(
//            code
//        )
        //project
//        // найдем что-то вроде "io.github.rtmigo:dec:0.0.1"
//        // и поменяем на актуальную версию
//        val readmeFile = project.rootDir.resolve("README.md")
//        val prefixToFind = "io.github.rtmigo:precise:"
//        val regex = """(?<=${Regex.escape(prefixToFind)})[0-9\.+]+""".toRegex()
//        val oldText = readmeFile.readText()
//        val newText = regex.replace(oldText, project.version.toString())
//        if (newText != oldText) readmeFile.writeText(newText)
    }
}

tasks.build {
    dependsOn("updateReadmeVersion")
}

tasks.register<Jar>("uberJar") {
    archiveClassifier.set("uber")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
             configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }
                 .map { zipTree(it) }
         })
}