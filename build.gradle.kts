import com.github.kevinah95.klizard.Configuration

plugins {
    kotlin("jvm") version "1.9.0"
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.25.3"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

apply(from = "${rootDir}/scripts/publish-module.gradle.kts")

mavenPublishing {
    val artifactId = "klizard"
    coordinates(
        Configuration.artifactGroup,
        artifactId,
        rootProject.extra.get("libVersion").toString()
    )

    pom {
        name.set(artifactId)
        description.set(
            "Kotlin version of a simple code complexity analyser " +
            "without caring about the C/C++ header files or Java imports, " +
            "supports most of the popular languages."
        )
    }
}