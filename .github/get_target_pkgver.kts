@file:DependsOn("com.lordcodes.turtle:turtle:0.5.0")

import com.lordcodes.turtle.*
import java.nio.file.Paths
import kotlin.concurrent.thread
import kotlin.system.exitProcess

fun <T:Any> timeLimited(seconds: Int, block: ()->T): T {
    var result: T? = null
    thread {
        result = block()
    }.join(seconds*1000L)
    if (result==null)
        throw Error("Failed to get the result withing the time specified")
    else
        return result!!
}

/**
 * Возвращает `true`, если репозитории гитхаба, соответствующем текущему `.git`,
 * уже существует релиз версии `version`.
 **/
fun releaseExists(version: String): Boolean = timeLimited(5) {
    try {
        shellRun("gh", listOf("release", "view", version))
        true
    } catch (e: ShellRunException) {
        if (e.errorText == "release not found")
            false
        else
            throw e
    }
}

fun String.looksLikeVersion() =
    """^\d+\.\d+.*$""".toRegex().matches(this)

/**
 * Полагаем, что в Gradle определён таск "pkgver", который печатает текущую
 * версию пакета. Запускаем Gradle, выясняем версию, возвращаем её.
 **/
fun currentPkgVer(): String =
    timeLimited(20) {  shellRun(
        Paths.get("gradlew").toAbsolutePath().toString(),
        listOf(
            "-quiet", "--no-daemon", "--console=plain",
            "pkgver")
    )}
        .lines().last()
        .also {
            require(it.looksLikeVersion()) { it }
        }

/**
 * Печатает текущую версию пакета Gradle, которая должна стать и версией релиза
 * гитхаб. Завершается ошибкой, если мы не может получить версию, либо версия
 * уже опубликована.
 **/
fun main() {
    val ver = currentPkgVer()
    if (releaseExists(ver)) {
        println("Release with version $ver already published.")
        exitProcess(1)
    }
    println(ver)
}

main()