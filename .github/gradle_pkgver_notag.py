import os.path
import re
import subprocess
import sys


def _gh_release_exists(ver: str) -> bool:  # не используется с 2022-10
    """Возвращает `true`, если репозитории гитхаба, соответствующем текущему
    `.git`, уже существует релиз версии `ver`. """
    try:
        # todo проверять те
        subprocess.check_output(["gh", "release", "view", ver],
                                stderr=subprocess.STDOUT)
        return True
    except subprocess.CalledProcessError as e:
        if e.output.strip() == b'release not found':
            return False
        raise


def _git_tag_exists(tag: str) -> bool:
    return subprocess.check_output(["git", "tag", "-l", tag]) \
               .decode().strip() != ""


def _gradle_pkgver() -> str:
    """Полагаем, что в Gradle определён таск "pkgver", который печатает текущую
       версию пакета. Запускаем Gradle, выясняем версию, возвращаем её."""
    ver = subprocess.check_output(
        [os.path.abspath("gradlew"), "-quiet", "--no-daemon", "--console=plain",
         "pkgver"],
        stderr=subprocess.STDOUT).splitlines()[-1].decode()
    assert re.match(r"^\d+\.\d+.*$", ver)
    return ver


def _print_unique_pkgver_or_throw():
    """Позволяет определить, готовы ли мы опубликовать текущий проект Gradle
    в качестве релиза на GitHub.

    Если да, то версию релиза печатаем в stdout.
    Если нет, то возвращаем код ошибки.

    Мы НЕ готовы публиковать, если такой релиз уже существует, либо если
    мы не смогли получить версию от Gradle.

    Эта функция может использоваться и непосредственно перед публикацией -
    и как предварительная проверка (чтобы не затевать долгий билд).
    """
    ver = _gradle_pkgver()
    if _git_tag_exists(ver):
        print(f"Tag {ver} already exists")
        sys.exit(1)
    print(ver)


if __name__ == "__main__":
    _print_unique_pkgver_or_throw()