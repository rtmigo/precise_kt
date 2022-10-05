////import org.gradle.kotlin.dsl.`kotlin-dsl`
//
//plugins {
//    `kotlin-dsl` // Is needed to turn our build logic written in Kotlin into Gralde Plugin
//}
//
//repositories {
//    gradlePluginPortal() // To use 'maven-publish' and 'signing' plugins in our own plugin
//}
//
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:1.5.21")
    //implementation("io.codearte.nexus-staging:0.30.0")
    implementation("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")// version "0.30.0"
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.7.10")
}
//
////gradlePlugin {
////    plugins {
////        register("hello-plugin") {
////            id = "hello"
////            implementationClass = "io.github.rtmigo.eldarg.HelloPlugin"
////        }
////    }
////}

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`



    id("org.jetbrains.dokka") version "1.7.10"

    //id("maven-publish") // maven
    //id("signing") // maven

    //id("java-library")
    //java
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}