from pathlib import Path
from rtmaven import maven_release_to_dir, Group, Artifact, Pom, GithubRepo, Developer

if __name__ == "__main__":
    maven_release_to_dir(
        Pom(group=Group("io.github.rtmigo"),
            artifact=Artifact("precise"),
            description="Kotlin/JVM compensated summation of Double sequences "
                        "to calculate sum, mean, standard deviation",
            license_name="MIT License",
            github=GithubRepo(owner="rtmigo", repo="precise_kt", branch="master"),
            devs=[Developer(
                name="Artsiom iG",
                email="ortemeo@gmail.com",
                organization="Revercode",
                organization_url="https://revercode.com")]),
        deploy_dir=Path(__file__).parent / "build" / "rtmaven")
