#!/usr/bin/env python3

from atomatr import update_gradle_ci
from pathlib import Path

# !!! с 2022 в worflows написанный вручную файл (не из atomatr)

def update():
    raise NotImplementedError
    update_gradle_ci(Path(__file__).parent, maven_central=True)


if __name__ == "__main__":
    update()
