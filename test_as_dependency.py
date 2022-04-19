import sys

from tempp import *

module="io.github.rtmigo:dec"

url="https://github.com/rtmigo/dec_kt"

code="""
    import io.github.rtmigo.dec.*
    import kotlinx.serialization.*
    import kotlinx.serialization.json.Json

    fun main() {
        Json.encodeToString(Dec(5.23))

        println(Dec(12.3))
    }
"""

try:
    imp_details = """{ version { branch = "__BRANCH__" } }""".replace("__BRANCH__", sys.argv[1])
except IndexError:
    imp_details = ""

with TempProject(
        files={
            # minimalistic build script to use the library
            "build.gradle.kts": """
                plugins {
                    id("application")
                    kotlin("jvm") version "1.6.20"
                    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.20"
                }

                repositories { mavenCentral() }
                application { mainClass.set("MainKt") }

                dependencies {
                    implementation("__MODULE__") __IMP_DETAILS__
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
                }
            """.replace("__MODULE__", module).replace("__IMP_DETAILS__", imp_details),

            # additional settings, if necessary
            "settings.gradle.kts": """
                sourceControl {
                    gitRepository(java.net.URI("__URL__.git")) {
                        producesModule("__MODULE__")
                    }
                }
            """.replace("__MODULE__", module).replace("__URL__", url),

            # kotlin code that imports and uses the library
            "src/main/kotlin/Main.kt": code}) as app:

    app.print_files()
    result = app.run(["gradle", "run", "-q"])

    print("returncode", result.returncode)

    print("stderr", "-"*80)
    print(result.stderr)

    print("stdout", "-"*80)
    print(result.stdout)
    print("-"*80)

    assert result.returncode == 0
    assert result.stdout == "12.3\n", result.stdout

print("Everything is OK!")