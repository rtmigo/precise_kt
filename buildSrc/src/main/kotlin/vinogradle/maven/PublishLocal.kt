package vinogradle.maven

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.*

abstract class PublishLocal : DefaultTask() {
    companion object {
        fun configure123(project: Project) {

            (project as org.gradle.api.plugins.ExtensionAware).extensions.configure<PublishingExtension>("publishing") {
                //project.extensions.getByType(PublishingExtension::class.java).apply {

                publications {

                    register<MavenPublication>("maven") {
                        from(project.components["java"])
                        //artifact(javadocJar)

                        pom {
//                name.set(projectName)
//                url.set("https://github.com/$ownerSlashRepo")
//                description.set(descriptionText)

                            developers {
                                developer {
                                    id.set("rtmigo")
                                    name.set("Artsiom iG")
                                    email.set("ortemeo@gmail.com")
                                }
                            }

//                licenses {
//                    license {
//                        name.set(licenseKind)
//                        url.set("https://github.com/$ownerSlashRepo/blob/-/LICENSE")
//                    }
//                }

//                scm {
//                    connection.set("scm:https://github.com/$ownerSlashRepo.git")
//                    developerConnection.set("scm:git@github.com:$ownerSlashRepo.git")
//                    url.set("https://github.com/$ownerSlashRepo")
//                }
                        }
                    }
                }
            }

        }
    }

    @TaskAction
    fun run() {

        (project as org.gradle.api.plugins.ExtensionAware).extensions.configure<PublishingExtension>("publishing") {
        //project.extensions.getByType(PublishingExtension::class.java).apply {

            publications {

                register<MavenPublication>("maven") {
                    from(project.components["java"])
                    //artifact(javadocJar)

                    pom {
//                name.set(projectName)
//                url.set("https://github.com/$ownerSlashRepo")
//                description.set(descriptionText)

                        developers {
                            developer {
                                id.set("rtmigo")
                                name.set("Artsiom iG")
                                email.set("ortemeo@gmail.com")
                            }
                        }

//                licenses {
//                    license {
//                        name.set(licenseKind)
//                        url.set("https://github.com/$ownerSlashRepo/blob/-/LICENSE")
//                    }
//                }

//                scm {
//                    connection.set("scm:https://github.com/$ownerSlashRepo.git")
//                    developerConnection.set("scm:git@github.com:$ownerSlashRepo.git")
//                    url.set("https://github.com/$ownerSlashRepo")
//                }
                    }
                }
            }
        }

        println("Configured?")

    }


}