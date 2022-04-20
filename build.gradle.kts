plugins {
    kotlin("jvm") version "1.6.20"
    id("java-library")
    java
}

group = "io.github.rtmigo"
version = "0.0.0+3"

tasks.register("pkgver") {
    doLast {
        println(project.version.toString())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("io.kotest:kotest-assertions-core:5.2.2")

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
        if (newText!=oldText)
            readmeFile.writeText(newText)
    }
}

tasks.build {
    dependsOn("updateReadmeVersion")
}


tasks.register<Jar>("uberJar") {
    archiveClassifier.set("uber")
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.INCLUDE

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
             configurations.runtimeClasspath.get()
                 .filter { it.name.endsWith("jar") }
                 .map { zipTree(it) }
         })
}
