# https://mccue.dev/pages/6-1-22-upload-to-maven-central?utm_source=pocket_mylist
import re
import shutil
import subprocess

from pathlib import Path
from typing import NewType, NamedTuple, Iterable


def print_header(text: str):
    print()
    print("#" * 80)
    print("  " + text.upper())
    print("#" * 80)
    print()


ProjectDir = NewType('ProjectDir', Path)
Artifact = NewType('PackageName', str)
Group = NewType('Group', str)
Version = NewType('Version', str)


def gradle(pd: ProjectDir) -> Path:
    return pd / "gradlew"


def gradle_clean(pd: ProjectDir):
    subprocess.check_call([gradle(pd), "clean"])


def project_dir_to_dokka_dir(project: ProjectDir) -> Path:
    return project / "build" / "dokka" / "html"


class PackageAttr(NamedTuple):
    artifact: Artifact
    version: Version


def docs_jar_basename(package: Artifact, version: Version) -> str:
    return f"{package}-{version}-javadoc.jar"


def create_docs(pd: ProjectDir, attrs: PackageAttr, target_dir: Path):
    print_header("Building javadoc.jar")
    assert not project_dir_to_dokka_dir(pd).exists()
    subprocess.check_call([gradle(pd), "dokkaHtml"])
    assert project_dir_to_dokka_dir(pd).exists()
    subprocess.check_call(
        ["jar", "--create", "--file", target_dir / docs_jar_basename(attrs.artifact, attrs.version),
         "-C", project_dir_to_dokka_dir(pd), "."])


def create_classes_and_sources_jar(pd: ProjectDir) -> list[Path]:
    print_header("Testing, building .jar and sources.jar")
    subprocess.check_call([gradle(pd), "build"])
    results = list(pd.glob("build/libs/*.jar"))
    assert len(results) == 2
    return results


def basename_to_name_ver(basename: str) -> PackageAttr[Artifact, Version]:
    # и имя пакета, и версия могут включать дефисы
    m = re.fullmatch(r"([-\w]+)-(\d.+)\.jar", basename)
    return PackageAttr(Artifact(m.group(1)), Version(m.group(2)))


def shortest_basename(paths: Iterable[Path]) -> str:
    return next(iter(sorted(paths, key=lambda f: len(f.artifact)))).name


class GithubRepo(NamedTuple):
    owner: str
    repo: str
    branch: str


class Developer(NamedTuple):
    name: str
    email: str
    organization: str
    organization_url: str


class Pom(NamedTuple):
    group: Group
    artifact: Artifact
    description: str

    license_name: str
    github: GithubRepo
    devs: list[Developer]
    name: str | None = None  # по умолчанию возьмём artifact
    homepage: str | None = None  # по умолчанию возьмём страницу гитхаба


def create_pom_xml(pom: Pom, version: Version, target_dir: Path):
    print_header("Creating POM")

    # здесь мы сделали допущение, что репозиторий точно на GitHub, а разработчик один
    if len(pom.devs) != 1:
        raise ValueError

    def github_url(r: GithubRepo):
        return f"https://github.com/{r.owner}/{r.repo}"

    xml = f"""
        <?xml version="1.0" encoding="UTF-8"?>
        <project xmlns="http://maven.apache.org/POM/4.0.0"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
            <modelVersion>4.0.0</modelVersion>
                        
            <groupId>{pom.group}</groupId>
            <artifactId>{pom.artifact}</artifactId>
            <version>{version}</version>
            <packaging>jar</packaging>
                        
            <name>{pom.name or pom.artifact}</name>
            <description>{pom.description}</description>
            <url>{pom.homepage or github_url(pom.github)}</url>
                        
            <licenses>
                <license>
                    <name>{pom.license_name}</name>
                    <url>https://github.com/{pom.github.owner}/{pom.github.repo}/blob/{pom.github.branch}/LICENSE</url>
                </license>
            </licenses>
                        
            <developers>
                <developer>
                    <name>{pom.devs[0].name}</name>
                    <email>{pom.devs[0].email}</email>
                    <organization>{pom.devs[0].organization}</organization>
                    <organizationUrl>{pom.devs[0].organization_url}</organizationUrl>
                </developer>
            </developers>
                        
            <scm>
                <connection>scm:git:git://github.com/{pom.github.owner}/{pom.github.repo}.git</connection>
                <developerConnection>scm:git:ssh://github.com:{pom.github.owner}/{pom.github.repo}.git</developerConnection>
                <url>https://github.com/{pom.github.owner}/{pom.github.repo}/tree/{pom.github.branch}</url>
            </scm>
        </project>    
    """

    basename = f"{pom.artifact}-{version}.pom"
    (target_dir / basename).write_text(xml.strip())


# def first_file_by_glob(d: Path, glob: str) -> Path:
#     for x in sorted(list(d.glob('*.jar'))):
#         if x.is_file():
#             return x
#     raise FileNotFoundError
#
#
# def dir_to_name_ver(d: Path) -> PackageAttr:
#     return basename_to_name_ver(first_file_by_glob(d, "*.jar").name)


# print(get_name_ver("package-0.1.0.jar"))
assert basename_to_name_ver("package-0.1.0.jar") == ("package", "0.1.0")
assert basename_to_name_ver("package-one-0.1.0.jar") == ("package-one", "0.1.0")
assert basename_to_name_ver("package-one-0.1.0-beta1.jar") == ("package-one", "0.1.0-beta1")


# exit()


def goo(pom: Pom):
    package = Artifact("stub")
    version = Version("0.0.0")
    project_dir = ProjectDir(Path(__file__).parent)

    deploy_dir = Path(__file__).parent / "tmp_deploy"
    if deploy_dir.exists():
        shutil.rmtree(deploy_dir)
    deploy_dir.mkdir()

    gradle_clean(project_dir)
    jars = create_classes_and_sources_jar(project_dir)
    attrs = basename_to_name_ver(shortest_basename(jars))

    if attrs.artifact!=pom.artifact:
        raise ValueError(f"Artifact mismatch: POM '{pom.artifact}', built '{attrs.artifact}'")

    for jar in create_classes_and_sources_jar(project_dir):
        jar.rename(deploy_dir / jar.name)
    create_docs(project_dir, attrs, deploy_dir)
    create_pom_xml(pom, attrs.version, deploy_dir)

    assert len(list(deploy_dir.glob("*"))) == 4


if __name__ == "__main__":
    goo(Pom(group=Group("io.github.rtmigo"), artifact=Artifact("precise"),
            description="Kotlin/JVM compensated summation of Double sequences "
                        "to calculate sum, mean, standard deviation", license_name="MIT License",
            github=GithubRepo(owner="rtmigo", repo="precise_kt", branch="master"), devs=[
            Developer(name="Artsiom iG", email="ortemeo@gmail.com", organization="Revercode",
                      organization_url="https://revercode.com")]))


