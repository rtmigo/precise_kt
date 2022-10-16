//#import vinogradle.maven.MavenMeta

//import vinogradle.publish.PublishToGithub

//import io.github.rtmigo.vinogradle.readme.*

plugins {
    kotlin("jvm") version "1.7.20"

    // Следующие плагины управляются из vinogradle, и их версии определяются в buildSrc.
    // Но подключить их нужно и здесь
    id("org.jetbrains.dokka") version "1.7.20"
    //id("signing")
    //id("io.codearte.nexus-staging") // "closeAndReleaseRepository"
    //id("maven-publish") // "publish"


    id("java-library")
    java
}

group = "io.github.rtmigo"
version = "0.1.0-dev20"

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
//#    maven { this. }

}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("io.kotest:kotest-assertions-core:5.5.0")
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


tasks.build {
    //dependsOn(updateReadme)
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
