#!/usr/bin/env python3

from atomatr import update_gradle_ci
from pathlib import Path


# работает без предварительных действий, если atomatr установлен как pip -e

def update():
    update_gradle_ci(Path(__file__).parent, maven_central=True)


if __name__ == "__main__":
    update()
