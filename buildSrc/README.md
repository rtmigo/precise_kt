# [vinogradle](https://github.com/rtmigo/vinogradle_kt)

Personal reusable Gradle build script for Kotlin/JVM. Used as a Git Submodule in
other projects.

### Add "buildSrc" as Git Subtree

```
cd /path/to/my_project_kt

git subtree add --prefix buildSrc https://github.com/rtmigo/vinogradle_kt dev --squash
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

### Update "buildSrc" from Git Subtree

```
cd /path/to/my_project_kt

git subtree pull --prefix buildSrc https://github.com/rtmigo/vinogradle_kt dev --squash -m "Pulling vinogradle from GitHub"

```
