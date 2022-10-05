package vinogradle.maven

import org.gradle.api.DefaultTask
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.*
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import java.net.URI
import org.jetbrains.dokka.gradle.DokkaTask
import vinogradle.maven.MavenPublishSettings


abstract class PublishToGithub : DefaultTask() {
    init {
        project.tasks.getByPath(":publish").dependsOn(this)
        //this.finalizedBy(project.tasks.getByPath(":publish"))
        //this.mustRunAfter(project.tasks.getByPath(":publish"))
        //val p = project.tasks.getByPath(":publish")
        //p.mustRunAfter(this)
        //p.shouldRunAfter(this)
        //this.dependsOn(p)
        //project.tasks.getByPath(":publish").shouldRunAfter(this)
        //this.s
    }

    @Input
    var settings: MavenPublishSettings? = null

    @Input
    var githubToken: String? = null

    @TaskAction
    fun printHere() {


        //.apply {
            //this.finalizedBy(ti)
            //this.shouldRunAfter(this)
//            actions.forEach {
//            println("Executing $it")
//                it.execute(this)
//            }
            //}
        //} // .find { it is DokkaTask }!! as DokkaTask


        println("Will publish to GitHub ${settings!!.ownerSlashRepo}")

        val dokkaHtml = project.tasks.find { it is DokkaTask }!! as DokkaTask

        //val dokkaHtml by project.tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
        val javadocJar: TaskProvider<Jar> by project.tasks.registering(Jar::class) {
            dependsOn(dokkaHtml)
            archiveClassifier.set("javadoc")
            from(dokkaHtml.outputDirectory)
        }

        project.extensions.getByType(PublishingExtension::class.java).apply {



            repositories {
                maven {
                    name = "GitHubPackages"
                    url = URI("https://maven.pkg.github.com/${settings!!.ownerSlashRepo}")
                    credentials {
                        username = settings!!.ownerSlashRepo.substringBefore("/")
                        password = this@PublishToGithub.githubToken
                    }
                }
            }

            publications {

                register<MavenPublication>("maven") {
                    from(project.components["java"])
                    artifact(javadocJar)

                    pom {
                        name.set(settings!!.projectName)
                        url.set("https://github.com/${settings!!.ownerSlashRepo}")
                        description.set(settings!!.descriptionText)

                        developers {
                            developer {
                                id.set("rtmigo")
                                name.set("Artsiom iG")
                                email.set("ortemeo@gmail.com")
                            }
                        }

                        licenses {
                            license {
                                name.set(settings!!.licenseKind)
                                url.set("https://github.com/${settings!!.ownerSlashRepo}/blob/-/LICENSE")
                            }
                        }

                        scm {
                            connection.set("scm:https://github.com/${settings!!.ownerSlashRepo}.git")
                            developerConnection.set("scm:git@github.com:${settings!!.ownerSlashRepo}.git")
                            url.set("https://github.com/${settings!!.ownerSlashRepo}")
                        }
                    }
                }
            }
        }

        //project.tasks.forEach {
        //    println(it.path)
        //}


        //Publisherx.
        //configurePublishing()
        //println(settings!!.ownerSlashRepo)
//        configurePublishing(
//            ownerSlashRepo = this.ownerSlashRepo!!

//        )
    }
}