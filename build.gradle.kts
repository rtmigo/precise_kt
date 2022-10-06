import vinogradle.maven.MavenMeta

//import vinogradle.publish.PublishToGithub

//import io.github.rtmigo.vinogradle.readme.*

plugins {
    kotlin("jvm") //version "1.7.20"

    // Следующие плагины управляются из vinogradle, и их версии определяются в buildSrc.
    // Но подключить их нужно и здесь
    id("org.jetbrains.dokka")
    id("signing")
    id("io.codearte.nexus-staging") // "closeAndReleaseRepository"
    id("maven-publish") // "publish"


    id("java-library")
    java
}

group = "io.github.rtmigo"
version = "0.1.0-dev5"

tasks.register("pkgver") {
    doLast {
        println(project.version.toString())
    }
}

java {
    withSourcesJar()  // для Maven Central
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

val updateReadme = tasks.register<vinogradle.readme.Installation>("updateReadme") {
    this.githubUrl = "https://github.com/rtmigo/precise_kt"
    this.mavenCentral = true
}

//logger.in

vinogradle.maven.Publishing.configure(
    project,
    MavenMeta(
    ownerSlashRepo = "rtmigo/precise_kt",
    projectName = "precise",
    license = "MIT License",
    description = "Kotlin/JVM compensated summation of Double sequences " +
        "to calculate sum, mean, standard deviation "),
    credentials = vinogradle.maven.MavenCredentials.fromEnv()
)

tasks.build {
    dependsOn(updateReadme)
}

tasks.register<Jar>("uberJar") {
    archiveClassifier.set("uber")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    //project

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
             configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }
                 .map { zipTree(it) }
         })
}