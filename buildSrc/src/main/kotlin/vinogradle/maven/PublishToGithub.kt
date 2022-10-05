package vinogradle.maven

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*


abstract class PublishToGithub : DefaultTask() {
    @Input
    var settings: MavenPublishSettings? = null

    @TaskAction
    fun printHere() {
        println(settings!!.ownerSlashRepo)
//        configurePublishing(
//            ownerSlashRepo = this.ownerSlashRepo!!

//        )
    }
}