import json
import subprocess
#from pathlib import Path

#import time
#from rtmaven import prepare, stage, promote, Package, eprint, eprint_header
from tempground import TempGround


def test_package(maven_url: str, ver: str):
    with TempGround(
            files={
                # minimalistic build script to use the library
                "build.gradle.kts": """
                    plugins {
                        id("application")
                        kotlin("jvm") version "1.6.20"
                    }
    
                    repositories { 
                        maven { url = uri("__TEMP_REPO__") }
                        mavenCentral() 
                    }
                    
                    application { mainClass.set("MainKt") }
    
                    dependencies {
                        implementation("io.github.rtmigo:precise:__VERSION__")
                    }
                """.replace("__VERSION__", ver)
                        .replace("__TEMP_REPO__", maven_url),

                # kotlin code that imports and uses the library
                "src/main/kotlin/Main.kt": """
                    import io.github.rtmigo.precise.*
                    
                    fun main() {
                        println(listOf(1.0, 2.0).preciseSumOf {it})
                    }
                """}
    ) as app:
        print(app.files_content())
        result = app.run(["gradle", "run", "-q"])
        print(result)

        assert result.returncode == 0
        assert result.stdout == "3.0\n", result.stdout

    print("Everything is OK!")


def build_test_release():
    js = json.loads(subprocess.check_output(
        ["java", "-jar", "/tmp/mavence.jar", "local"]))
    test_package(js["mavenRepo"], js["version"])
    # package = stage(prepare(
    #     description="Kotlin/JVM compensated summation of Double sequences "
    #                 "to calculate sum, mean, standard deviation",
    #     github_url="https://github.com/rtmigo/precise_kt@master",
    #     developer="Artsiom iG <ortemeo@gmail.com>",
    #     license="MIT"))
    #
    # eprint_header("Testing")
    # eprint()
    # test_package(package)
    # promote(package)


if __name__ == "__main__":
    build_test_release()
