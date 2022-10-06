#!/bin/bash
set -e && cd "${0%/*}"

git subtree pull --prefix buildSrc https://github.com/rtmigo/vinogradle_kt dev --squash -m "Pulling vinogradle from GitHub"