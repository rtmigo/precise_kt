from pathlib import Path

import time
from rtmaven import maven_release_to_dir, Group, Artifact, StagingLib, Artifact, ProjectMeta, \
    GithubRepo, \
    Developer, eprint, eprint_header
from tempp import TempProject


def test_import_from_maven(staging_lib: StagingLib):
    # module = "io.github.rtmigo:precise:0.1.0-dev19"
    with TempProject(
            files={
                # minimalistic build script to use the library
                "build.gradle.kts": """
                    plugins {
                        id("application")
                        kotlin("jvm") version "1.6.20"
                    }
    
                    repositories { 
                        maven { url = uri("__REPO__") }
                        mavenCentral() 
                    }
                    application { mainClass.set("MainKt") }
    
                    dependencies {
                        implementation("__MODULE__")
                    }
                """.replace("__MODULE__", str(staging_lib.library)).replace("__REPO__",
                                                                            staging_lib.maven_url),

                # kotlin code that imports and uses the library
                "src/main/kotlin/Main.kt": """
                    import io.github.rtmigo.precise.*
                
                    fun main() {
                        println(listOf(1.0, 2.0).preciseSumOf {it})
                    }
                """}
    ) as app:
        eprint(app.files_content())
        result = app.run(["gradle", "run", "-q"])

        eprint("returncode", result.returncode)

        eprint("stderr", "-" * 80)
        eprint(result.stderr)

        eprint("stdout", "-" * 80)
        eprint(result.stdout)
        eprint("-" * 80)

        assert result.returncode == 0
        assert result.stdout == "3.0\n", result.stdout

    eprint("Everything is OK!")


# test_import_from_maven(
#     maven_repo_url="https://s01.oss.sonatype.org/content/repositories/iogithubrtmigo-1014/",
#     artifact=Artifact.parse("io.github.rtmigo:precise:0.1.0-dev19")
# )

if __name__ == "__main__":
    staging = maven_release_to_dir(
        ProjectMeta(group=Group("io.github.rtmigo"),
                    artifact=Artifact("precise"),
                    description="Kotlin/JVM compensated summation of Double sequences "
                                "to calculate sum, mean, standard deviation",
                    license_name="MIT License",
                    github=GithubRepo(owner="rtmigo", repo="precise_kt", branch="master"),
                    devs=[Developer(
                        name="Artsiom iG",
                        email="ortemeo@gmail.com")
                        #    organization="Revercode",
                        #   organization_url="https://revercode.com")
                    ]),
        #    deploy_dir=Path(__file__).parent / "build" / "rtmaven"
    )

    eprint_header("Testing staging")
    eprint()
    test_import_from_maven(staging)

