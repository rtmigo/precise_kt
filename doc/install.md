## Gradle (Kotlin)

```kotlin
repositories {
    mavenCentral()
}                

dependencies {
    implementation("io.github.rtmigo:precise:0.0.0")
}    
```

## Gradle (Groovy)

```groovy
repositories {
    mavenCentral()
}                

dependencies {
    implementation "io.github.rtmigo:precise:0.0.0"
}
```

<details>
  <summary>Maven</summary>
     
    ## Maven
    
    ```xml    
    <dependencies>
        <dependency>
            <groupId>io.github.rtmigo</groupId>
            <artifactId>precise</artifactId>
            <version>0.0.0</version>
        </dependency>
    </dependencies>
    ```
</details>

## Latest (Kotlin Gradle) 

#### settings.gradle.kts

```kotlin
sourceControl {
    gitRepository(java.net.URI("https://github.com/rtmigo/precise_kt.git")) {
        producesModule("io.github.rtmigo:precise")
    }
}
```

#### build.gradle.kts

```kotlin
dependencies {
    implementation("io.github.rtmigo:precise") {
        version { branch = "staging" }
    }
}
```