# [vinogradle](https://github.com/rtmigo/vinogradle_kt)

Personal reusable Gradle build script for Kotlin/JVM. Used as a Git Submodule in
other projects.

### Add as "buildSrc" Git Submodule

```
cd /path/to/big_project_kt

git submodule add -b dev https://github.com/rtmigo/vinogradle_kt ./buildSrc
```

### Edit project dependencies

```kotlin
plugins {
    kotlin("jvm")  // not specifying version

    id("org.jetbrains.dokka")
    id("signing")
    id("io.codearte.nexus-staging")  // for "closeAndReleaseRepository"
    id("maven-publish")  // for "publish"
}
```

### Add step to GitHub Actions:

```yml
- name: Update submodules
  run: git submodule update --init --recursive
```