# KLizard

[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kevinah95/klizard)](https://central.sonatype.com/artifact/io.github.kevinah95/klizard)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/io.github.kevinah95/klizard?server=https%3A%2F%2Fs01.oss.sonatype.org)](https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/kevinah95/klizard)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fkevinah95%2FKLizard.svg?type=shield&issueType=license)](https://app.fossa.com/projects/git%2Bgithub.com%2Fkevinah95%2FKLizard?ref=badge_shield&issueType=license)

KLizard is an extensible Cyclomatic Complexity Analyzer for many programming languages including C/C++ (doesn't require all the header files or Java imports). It also does copy-paste detection (code clone detection/code duplicate detection) and many other forms of static code analysis.

## About this repository

This project is a kotlin implementation of *Lizard* by Terry Yin, originally from
[Lizard](https://github.com/terryyin/lizard).

## Installation

### with Gradle

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.kevinah95:klizard:[version]")
}
```

## License
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fkevinah95%2FKLizard.svg?type=large&issueType=license)](https://app.fossa.com/projects/git%2Bgithub.com%2Fkevinah95%2FKLizard?ref=badge_large&issueType=license)