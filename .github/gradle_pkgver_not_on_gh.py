################################################################################
#
#            BEFORE EDITING MAKE SURE THIS IS NOT A SYNCED COPY
#
################################################################################

import os.path
import re
import subprocess
import sys


def _release_exists(ver: str) -> bool:
    """Возвращает `true`, если репозитории гитхаба, соответствующем текущему `.git`,
       уже существует релиз версии `ver`."""
    try:
        subprocess.check_output(["gh", "release", "view", ver],
                                stderr=subprocess.STDOUT)
        return True
    except subprocess.CalledProcessError as e:
        if e.output.strip() == b'release not found':
            return False
        raise


def _current_pkgver() -> str:
    """Полагаем, что в Gradle определён таск "pkgver", который печатает текущую
       версию пакета. Запускаем Gradle, выясняем версию, возвращаем её."""
    ver = subprocess.check_output(
        [os.path.abspath("gradlew"), "-quiet", "--no-daemon", "--console=plain",
         "pkgver"],
        stderr=subprocess.STDOUT).splitlines()[-1].decode()
    assert re.match(r"^\d+\.\d+.*$", ver)
    return ver


def target_version_to_stdout():
    """Позволяет определить, готовы ли мы опубликовать текущий проект Gradle
    в качестве релиза на GitHub.

    Если да, то версию релиза печатаем в stdout.
    Если нет, то возвращаем код ошибки.

    Мы НЕ готовы публиковать, если такой релиз уже существует, либо если
    мы не смогли получить версию от Gradle.

    Эта функция может использоваться и непосредственно перед публикацией -
    и как предварительная проверка (чтобы не затевать долгий билд).
    """
    ver = _current_pkgver()
    if _release_exists(ver):
        print(f"Release {ver} already exists")
        sys.exit(1)
    print(ver)


if __name__ == "__main__":
    target_version_to_stdout()
