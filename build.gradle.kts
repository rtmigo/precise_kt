import vinogradle.maven.MavenMeta

//import vinogradle.publish.PublishToGithub

//import io.github.rtmigo.vinogradle.readme.*

plugins {
    kotlin("jvm") //version "1.7.20"

    id("org.jetbrains.dokka") //version "1.7.10"
    id("io.codearte.nexus-staging") //version "0.30.0"
    id("maven-publish") // maven
    id("signing") // maven

    id("java-library")
    java
    //`wtf-convention`
//    hello

    //id("convention.readme")
}




group = "io.github.rtmigo"
version = "0.1.0-dev4"

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



tasks.register("genInstallation") {
    dependsOn("hi")

    doFirst {

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
