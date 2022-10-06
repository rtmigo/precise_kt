#!/bin/bash
set -e && cd "${0%/*}"

git add .
git commit -m "Before pulling vinogradle" --allow-empty
git subtree pull --prefix buildSrc https://github.com/rtmigo/vinogradle_kt dev --squash -m "Pulling vinogradle from GitHub"