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
}

repositories {
    mavenCentral()
}