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

val sonatypeUsername by lazy { System.getenv("SONATYPE_USERNAME")!! } // можно токен
val sonatypePassword by lazy { System.getenv("SONATYPE_PASSWORD")!! } // можно токен


publishing {
    val ownerSlashRepo = "rtmigo/precise_kt"

    val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
    val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaHtml.outputDirectory)
    }

    val projectName = "precise" // наверно как-то можно получить из мета-данных
    val licenseKind = "MIT License"
    val descrText = "Kotlin/JVM compensated summation of Double sequences " +
        "to calculate sum, mean, standard deviation "
    val publishToGitHub = false
    val publishToSonatype = true

    //val sonatypeUsername by lazy {}

    repositories {
        if (publishToGitHub)
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/$ownerSlashRepo")

                credentials {
                    username =
                        project.findProperty("gpr.user") as String?
                            ?: System.getenv("GITHUB_USERNAME")
                                ?: ownerSlashRepo.substringBefore("/")
                    password = project.findProperty("gpr.key") as String?
                        ?: System.getenv("GITHUB_PACKAGE_PUBLISHING_TOKEN")
                }
            }

        if (publishToSonatype)
            maven {
                val releasesUrl =
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsUrl =
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl
                credentials {
                    username = sonatypeUsername // можно токен
                    password = sonatypePassword // можно токен
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
                description.set(descrText)

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
                        url.set("https://github.com/$ownerSlashRepo/blob/staging/LICENSE")
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

nexusStaging {
    // конфигурируем плагин id("io.codearte.nexus-staging").
    // Задачу можно будет запустить так: ./gradlew closeAndReleaseRepository
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
    username = sonatypeUsername
    password = sonatypePassword
}

signing {
    // Using the following setup, you can pass the secret key (in ascii-armored format) and the
    // password using the ORG_GRADLE_PROJECT_signingKey and ORG_GRADLE_PROJECT_signingPassword
    // environment variables, respectively (https://bit.ly/3CuTCdD)
    val signingKey: String? by project
    val signingPassword: String? by project

    //println("Signing with key ${signingKey}")
    //println("Signing with pwd ${signingPassword}")

    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}



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

tasks.register("prop") {
    doFirst {
        println(project.findProperty("abc"))
    }
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
        if (newText != oldText)
            readmeFile.writeText(newText)
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
             configurations.runtimeClasspath.get()
                 .filter { it.name.endsWith("jar") }
                 .map { zipTree(it) }
         })
}