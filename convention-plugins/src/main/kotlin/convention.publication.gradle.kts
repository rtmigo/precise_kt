//
//import org.gradle.api.publish.maven.MavenPublication
//import org.gradle.api.tasks.bundling.Jar
//import org.gradle.kotlin.dsl.`maven-publish`
//import org.gradle.kotlin.dsl.signing
//import java.util.*
//
//plugins {
//    `maven-publish`
//    signing
//}
//
///**
// * В этом объекте объединены реквизиты Sonatype и GPG.
// *
// * Потому что подписывать пакеты GPG мы будем только при публикации в Sonatype,
// * но не GitHub Packages.
// **/
//data class SonatypeCredentials(
//    /** Имя пользователя Sonatype, либо токен username */
//    val username: String = System.getenv("SONATYPE_USERNAME")!!,
//    /** Пароль Sonatype, либо токен password */
//    val password: String = System.getenv("SONATYPE_PASSWORD")!!,
//    /** ASCII armored key */
//    val gpgPrivateKey: String = System.getenv("MAVEN_GPG_KEY")!!,
//    /** Пароль для дешифровки из [gpgPrivateKey] */
//    val gpgPassword: String = System.getenv("MAVEN_GPG_PASSWORD")!!,
//)
//
////data class GithubConfig(
////    val username: String = System.getenv("GITHUB_USERNAME"),
////    val password: String  = System.getenv("GITHUB_PKGPUB_TOKEN")
////)
//
//fun org.gradle.api.Project.configurePublishing(
//    ownerSlashRepo: String,
//    licenseKind: String,
//    projectName: String,
//    descriptionText: String,
//    sonatype: SonatypeCredentials? = null,
//    githubToken: String? = null,
//) {
//    if (sonatype == null && githubToken == null) return
//
//    //require(sonatype!=null || githubToken!=null)
//
//    this.publishing {
//        //val ownerSlashRepo = "rtmigo/precise_kt"
//
//        val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
//        val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
//            dependsOn(dokkaHtml)
//            archiveClassifier.set("javadoc")
//            from(dokkaHtml.outputDirectory)
//        }
//
//        //val projectName = "precise" // наверно как-то можно получить из мета-данных
//        //val licenseKind = "MIT License"
//        //val descrText = "Kotlin/JVM compensated summation of Double sequences " +
//        //   "to calculate sum, mean, standard deviation "
//        //val publishToGitHub = false
//        //val publishToSonatype = true
//
//        //val sonatypeUsername by lazy {}
//
//        repositories {
//            if (githubToken != null) maven {
//                name = "GitHubPackages"
//                url = uri("https://maven.pkg.github.com/$ownerSlashRepo")
//
//                credentials {
//                    username = ownerSlashRepo.substringBefore("/")
////                            project.findProperty("gpr.user") as String?
////                                ?: System.getenv("GITHUB_USERNAME")
////                                    ?: ownerSlashRepo.substringBefore("/")
//                    password = githubToken
////                            project.findProperty("gpr.key") as String?
////                          ?: System.getenv("GITHUB_PACKAGE_PUBLISHING_TOKEN")
//                }
//            }
//
//            if (sonatype != null) maven {
//                val releasesUrl =
//                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
//                val snapshotsUrl =
//                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
//                url = (if (version.toString().endsWith("SNAPSHOT"))
//                    snapshotsUrl
//                else
//                    releasesUrl)
//                credentials {
//                    username = sonatype.username // можно токен
//                    password = sonatype.password // можно токен
//                }
//            }
//        }
//
//        publications {
//
//            register<MavenPublication>("maven") {
//                from(components["java"])
//                artifact(javadocJar)
//
//                pom {
//                    name.set(projectName)
//                    url.set("https://github.com/$ownerSlashRepo")
//                    description.set(descriptionText)
//
//                    developers {
//                        developer {
//                            id.set("rtmigo")
//                            name.set("Artsiom iG")
//                            email.set("ortemeo@gmail.com")
//                        }
//                    }
//
//                    licenses {
//                        license {
//                            name.set(licenseKind)
//                            url.set("https://github.com/$ownerSlashRepo/blob/-/LICENSE")
//                        }
//                    }
//
//                    scm {
//                        connection.set("scm:https://github.com/$ownerSlashRepo.git")
//                        developerConnection.set("scm:git@github.com:$ownerSlashRepo.git")
//                        url.set("https://github.com/$ownerSlashRepo")
//                    }
//                }
//            }
//        }
//    }
//
//    if (sonatype != null) {
//        nexusStaging {
//            // конфигурируем плагин id("io.codearte.nexus-staging").
//            // Задачу можно будет запустить так: ./gradlew closeAndReleaseRepository
//            serverUrl = "https://s01.oss.sonatype.org/service/local/"
//            username = sonatype.username
//            password = sonatype.password
//        }
//
//        signing {
//            // Using the following setup, you can pass the secret key (in ascii-armored format) and the
//            // password using the ORG_GRADLE_PROJECT_signingKey and ORG_GRADLE_PROJECT_signingPassword
//            // environment variables, respectively (https://bit.ly/3CuTCdD)
//            //val signingKey: String? by project
//            //val signingPassword: String? by project
//
//            //println("Signing with key ${signingKey}")
//            //println("Signing with pwd ${signingPassword}")
//
//            useInMemoryPgpKeys(sonatype.gpgPrivateKey, sonatype.gpgPassword)
//            sign(publishing.publications["maven"])
//        }
//    }
//}
//
////tasks.register<Jar>("publish") {
//
////}
//
//abstract class PublishToGithub222 : DefaultTask() {
//    @Action
//    fun printHere() {
//            println("aaaa")
//    }
//}