import os.path
import re
import subprocess
import sys


def release_exists(ver: str) -> bool:
    """Возвращает `true`, если репозитории гитхаба, соответствующем текущему `.git`,
       уже существует релиз версии `ver`."""
    try:
        subprocess.check_output(["gh", "release", "view", ver], stderr=subprocess.STDOUT)
        return True
    except subprocess.CalledProcessError as e:
        if e.output.strip() == b'release not found':
            return False
        raise


def print_target_version():
    ver = current_pkgver()
    if release_exists(ver):
        print(f"Release {ver} already exists")
        sys.exit(1)
    print(ver)


def current_pkgver() -> str:
    """Полагаем, что в Gradle определён таск "pkgver", который печатает текущую
       версию пакета. Запускаем Gradle, выясняем версию, возвращаем её."""
    ver = subprocess.check_output(
        [os.path.abspath("gradlew"), "-quiet", "--no-daemon", "--console=plain", "pkgver"],
        stderr=subprocess.STDOUT).splitlines()[-1].decode()
    assert re.match(r"^\d+\.\d+.*$", ver)
    return ver


if __name__ == "__main__":
    print_target_version()
