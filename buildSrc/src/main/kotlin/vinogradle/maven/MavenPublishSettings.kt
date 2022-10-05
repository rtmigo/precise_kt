package vinogradle.maven

data class MavenPublishSettings(
    val ownerSlashRepo: String,
    val licenseKind: String,
    val projectName: String,
    val descriptionText: String,
) {
    init {
        require(ownerSlashRepo.count { it == '/' } == 1)
    }
}
