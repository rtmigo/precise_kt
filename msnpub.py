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
PackageName = NewType('PackageName', str)
Version = NewType('Version', str)


def gradle(pd: ProjectDir) -> Path:
    return pd / "gradlew"


def gradle_clean(pd: ProjectDir):
    subprocess.check_call([gradle(pd), "clean"])


def project_dir_to_dokka_dir(project: ProjectDir) -> Path:
    return project / "build" / "dokka" / "html"


class PackageAttr(NamedTuple):
    name: PackageName
    version: Version


def docs_jar_basename(package: PackageName, version: Version) -> str:
    return f"{package}-{version}-javadoc.jar"


def create_docs(pd: ProjectDir, attrs: PackageAttr, target_dir: Path):
    print_header("Building javadoc.jar")
    assert not project_dir_to_dokka_dir(pd).exists()
    subprocess.check_call([gradle(pd), "dokkaHtml"])
    assert project_dir_to_dokka_dir(pd).exists()
    subprocess.check_call(
        ["jar", "--create",
         "--file", target_dir / docs_jar_basename(attrs.name, attrs.version),
         "-C", project_dir_to_dokka_dir(pd), "."])


def create_classes_and_sources_jar(pd: ProjectDir) -> list[Path]:
    print_header("Testing, building .jar and sources.jar")
    subprocess.check_call([gradle(pd), "build"])
    results = list(pd.glob("build/libs/*.jar"))
    assert len(results) == 2
    return results


def basename_to_name_ver(basename: str) -> PackageAttr[PackageName, Version]:
    # и имя пакета, и версия могут включать дефисы
    m = re.fullmatch(r"([-\w]+)-(\d.+)\.jar", basename)
    return PackageAttr(PackageName(m.group(1)), Version(m.group(2)))


def shortest_basename(paths: Iterable[Path]) -> str:
    return next(iter(sorted(paths, key=lambda f: len(f.name)))).name


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


def goo():
    package = PackageName("stub")
    version = Version("0.0.0")
    project_dir = ProjectDir(Path(__file__).parent)

    deploy_dir = Path(__file__).parent / "tmp_deploy"
    if deploy_dir.exists():
        shutil.rmtree(deploy_dir)
    deploy_dir.mkdir()

    gradle_clean(project_dir)
    jars = create_classes_and_sources_jar(project_dir)
    attrs = basename_to_name_ver(shortest_basename(jars))

    for jar in create_classes_and_sources_jar(project_dir):
        jar.rename(deploy_dir / jar.name)
    create_docs(project_dir, attrs, deploy_dir)


if __name__ == "__main__":
    goo()
