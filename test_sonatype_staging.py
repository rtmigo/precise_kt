import sys

from tempp import *


def test_import_from_maven(
        maven_repo_url: str,
        version: str):
    module = "io.github.rtmigo:precise:0.1.0-dev19"
    with TempProject(
            files={
                # minimalistic build script to use the library
                "build.gradle.kts": """
                    plugins {
                        id("application")
                        kotlin("jvm") version "1.6.20"
                    }
    
                    repositories { 
                        maven { url = uri(f"__REPO__") }
                        mavenCentral() 
                    }
                    application { mainClass.set("MainKt") }
    
                    dependencies {
                        implementation("__MODULE__")
                    }
                """.replace("__MODULE__", module).replace("__REPO__", maven_repo_url),

                # kotlin code that imports and uses the library
                "src/main/kotlin/Main.kt": """
                    import io.github.rtmigo.precise.*
                
                    fun main() {
                        println(listOf(1.0, 2.0).preciseSumOf {it})
                    }
                """}
    ) as app:
        print(app)
        result = app.run(["gradle", "run", "-q"])

        print("returncode", result.returncode)

        print("stderr", "-" * 80)
        print(result.stderr)

        print("stdout", "-" * 80)
        print(result.stdout)
        print("-" * 80)

        assert result.returncode == 0
        assert result.stdout == "3.0\n", result.stdout

    print("Everything is OK!")


test_import_from_maven(
    maven_repo_url="https://s01.oss.sonatype.org/content/repositories/iogithubrtmigo-1014/",

)
