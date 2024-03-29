import sys

from tempground import *

module="io.github.rtmigo:precise"

url="https://github.com/rtmigo/precise_kt"

code="""
    import io.github.rtmigo.precise.*

    fun main() {
        println(listOf(1.0, 2.0).preciseSumOf {it})
    }
"""

try:
    imp_details = """{ version { branch = "__BRANCH__" } }""".replace("__BRANCH__", sys.argv[1])
except IndexError:
    imp_details = ""

with TempGround(
        files={
            # minimalistic build script to use the library
            "build.gradle.kts": """
                plugins {
                    id("application")
                    kotlin("jvm") version "1.6.20"
                }

                repositories { mavenCentral() }
                application { mainClass.set("MainKt") }

                dependencies {
                    implementation("__MODULE__") __IMP_DETAILS__
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

    print(app.files_content())
    result = app.run(["gradle", "run", "-q"])
    print(result)

    assert result.returncode == 0
    assert result.stdout == "3.0\n", result.stdout

print("Everything is OK!")
