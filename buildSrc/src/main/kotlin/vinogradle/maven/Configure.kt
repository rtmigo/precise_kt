package vinogradle.maven

import io.codearte.gradle.nexus.NexusStagingExtension
import org.gradle.api.*
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI


data class MavenMeta(
    val ownerSlashRepo: String,
    val license: String,
    val projectName: String,
    val description: String,
)

class EnvKeyNotFoundException(key: String): Exception("Env does not have non-blank '$key' defined.")

fun getenvOrThrow(key: String): String {
    val result = System.getenv(key)
    if (result == null || result.isBlank())
        throw EnvKeyNotFoundException(key)
    return result
}

sealed class MavenCredentials {
    companion object {
        /**
         * Генерирует экземпляр [MavenCredentials], конкретный тип которого зависит от заданных
         * переменных среды.
         **/
        fun fromEnv(): MavenCredentials {
            fun tryCreate(block: () -> MavenCredentials) =
                try {
                    block()
                } catch (_: EnvKeyNotFoundException) {
                    null
                }

            val results = listOfNotNull(
                tryCreate { GithubCredentials.fromEnv() },
                tryCreate { SonatypeCredentials.fromEnv() },
            )

            return when (results.size) {
                0 -> LocalCredentials
                1 -> results.single()
                else ->
                    throw Error("Env contains ${results.size} possible matching variables")
            }.also {
                println("Auto-detected ${MavenCredentials::class.simpleName} type: " +
                            "${it::class.simpleName}")
            }
        }
    }
}

data class GithubCredentials(
    var token: String,
) : MavenCredentials() {
    companion object {
        fun fromEnv() = GithubCredentials(getenvOrThrow("GITHUB_PKGPUB_TOKEN"))
    }
}

object LocalCredentials : MavenCredentials()

/**
 * В этом объекте объединены реквизиты Sonatype и GPG, поскольку использую я их только вместе.
 * Sonatype требует подписей GPG для публикаций в Maven Central. Другие публикации в Maven подписей
 * не требуют.
 **/
data class SonatypeCredentials(
    /** Имя пользователя Sonatype, либо токен username */
    val username: String,
    /** Пароль Sonatype, либо токен password */
    val password: String,
    /** ASCII armored key */
    val gpgPrivateKey: String,
    /** Пароль для дешифровки из [gpgPrivateKey] */
    val gpgPassword: String,
) : MavenCredentials() {
    companion object {
        fun fromEnv() = SonatypeCredentials(
            username = getenvOrThrow("SONATYPE_USERNAME"),
            password = getenvOrThrow("SONATYPE_PASSWORD"),
            gpgPrivateKey = getenvOrThrow("MAVEN_GPG_KEY"),
            gpgPassword = getenvOrThrow("MAVEN_GPG_PASSWORD"))
    }
}

/**
 * 2022-10 Я выяснил, что конфигурирование блоком `publishing { }` действует на таски `publish` и
 * `publishToMavenLocal` только если вписывается прямо в тело `build.gradle.kts` (то есть,
 * выполняется первым делом). Если сделать это с запаздыванием, например, из собственного таска
 * (`doFirst` или `MyTask: Task`) - то конфигурация запоминается внутри `Project`, но `publish` и
 * `publishToMavenLocal` ничего не публикуют.
 *
 * То есть, (пока) не получится создать таски:
 * ```
 *   ./gradlew publishToGithub
 *   ./gradlew configForGithub publish
 * ```
 *
 * Поэтому следующий код описан не как задача, а как функции - чтобы запустить их из тела
 * `build.gradle.kts`. Причём, для каждого запуска `gradlew` вызов функции допустим ровно один раз.
 *
 * Это накладывает ограничение: при отдельном запуске `gradlew` мы сможем обновить не более одного
 * репозитория. Чтобы отправить проект в три репозитория, нужно будет написать скрипт снаружи
 * `gradlew` - который запустит три команды.
 *
 * Технически можно было бы внутри `publishing` задать сразу несколько целевых репозиториев, чтобы
 * отправлять одним махом на Sonatype и GitHub Packages. Но к надежности и простоте тестирования
 * это будет минус. К тому же, пакеты нужно генерировать слегка разные (Sonatype с GPG-подписью),
 * и этапов разное количество. В общем, это путь неоправданного объединения сущностей.
 *
 * Можно внутри Gradle сделать конфигурирование зависимым от наличия переменных среды,
 * а далее запускать, условно говоря, так:
 *
 * Maven Central:
 * ```bash
 *   SONATYPE_CREDENTIALS=abcd ./gradlew publish closeAndReleaseRepository
 * ```
 *
 * GitHub:
 * ```bash
 *   GITHUB_CREDENTIALS=abcd ./gradlew publish
 * ```
 **/
object Publishing {

    private var configured = false

    private fun <T> Project.configureNamed(name: String, func: Action<T>) {
        (this as org.gradle.api.plugins.ExtensionAware).extensions
            .configure<T>(name, func)
    }

    private fun Project.publishingBlock(func: Action<PublishingExtension>) =
        this.configureNamed("publishing", func)

    private fun Project.nexusStagingBlock(func: Action<NexusStagingExtension>) =
        this.configureNamed("nexusStaging", func)

    private fun Project.signingBlock(func: Action<SigningExtension>) =
        this.configureNamed("signing", func)

    private val Project.publishingExt: PublishingExtension
        get() = (this as org.gradle.api.plugins.ExtensionAware)
            .extensions.getByName("publishing") as PublishingExtension

    private fun javadocJar(project: Project) =
        project.tasks.create("javaDocJar", Jar::class.java) {
            //println(project.tasks.filter { it is DokkaTask })
            val dokkaHtml = project.tasks.getByPath(":dokkaHtml") as DokkaTask //project.tasks.filter { it is DokkaTask }.single() as DokkaTask
            this.dependsOn(dokkaHtml)
            this.archiveClassifier.set("javadoc")
            this.from(dokkaHtml.outputDirectory)
        }

    /**
     * В зависимости от типа экземпляра [credentials] конфигурируем `publish` для публикаций
     * на Maven Central, GitHub Packages или локально.
     **/
    public fun configure(
        project: Project,
        meta: MavenMeta,
        credentials: MavenCredentials,
    ) {
        println("${(::configure)::name.get()} called for $meta and ${credentials::class}")

        if (this.configured) {
            println("WARNING: ${(::configure)::name.get()} running again")
        }
        this.configured = true

        val jdoc = javadocJar(project)

        project.publishingBlock {
            repositories {
                when (credentials) {
                    is LocalCredentials -> {
                        // pass
                    }
                    is GithubCredentials ->
                        maven {
                            name = "GitHubPackages"
                            url = URI("https://maven.pkg.github.com/${meta.ownerSlashRepo}")

                            credentials {
                                username = meta.ownerSlashRepo.substringBefore("/")
                                password = credentials.token
                            }
                        }
                    is SonatypeCredentials ->
                        maven {
                            url = if (project.version.toString().endsWith("SNAPSHOT"))
                                URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                            else
                                URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                            credentials {
                                username = credentials.username // можно токен
                                password = credentials.password // можно токен
                            }
                        }
                }
            }
            publications {

                register<MavenPublication>("maven", MavenPublication::class.java) {
                    from(project.components.getByName("java"))
                    artifact(jdoc)

                    pom {
                        name.set(meta.projectName)
                        url.set("https://github.com/${meta.ownerSlashRepo}")
                        description.set(meta.description)

                        developers {
                            developer {
                                id.set("rtmigo")
                                name.set("Artsiom iG")
                                email.set("ortemeo@gmail.com")
                            }
                        }

                        licenses {
                            license {
                                name.set(meta.license)
                                url.set("https://github.com/${meta.ownerSlashRepo}/blob/-/LICENSE")
                            }
                        }

                        scm {
                            connection.set("scm:https://github.com/${meta.ownerSlashRepo}.git")
                            developerConnection.set("scm:git@github.com:${meta.ownerSlashRepo}.git")
                            url.set("https://github.com/${meta.ownerSlashRepo}")
                        }
                    }
                }
            }
        }

        if (credentials is SonatypeCredentials) {
            project.nexusStagingBlock {
                // конфигурируем плагин id("io.codearte.nexus-staging").
                // Задачу можно будет запустить так: ./gradlew closeAndReleaseRepository
                serverUrl = "https://s01.oss.sonatype.org/service/local/"
                username = credentials.username
                password = credentials.password
            }

            project.signingBlock {
                useInMemoryPgpKeys(credentials.gpgPrivateKey, credentials.gpgPassword)
                sign(project.publishingExt.publications["maven"])
            }
        }
    }
}
