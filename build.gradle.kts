plugins {
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.dokka") version "1.7.20"
    id("java-library")
    java
    id("maven-publish")
}


lateinit var central: Publication

publishing {
    publications {
        central = create<MavenPublication>("cenTrAl") {
            from(components["java"])
            pom {
                val repo = "precise_kt"
                name.set("precise")
                description.set("Kotlin/JVM compensated summation of Double " +
                                    "sequences to calculate sum, mean, standard deviation")
                url.set("https://github.com/rtmigo/$repo")
                developers {
                    developer {
                        name.set("Artsiom iG")
                        email.set("ortemeo@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git://github.com/rtmigo/$repo.git")
                    url.set("https://github.com/rtmigo/$repo")
                }
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/rtmigo/$repo/blob/HEAD/LICENSE")
                    }
                }
            }
        }
    }
}

group = "io.github.rtmigo"
version = "0.1.0"

java {
    withSourcesJar()  // для Maven Central
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("io.kotest:kotest-assertions-core:5.5.1")
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
