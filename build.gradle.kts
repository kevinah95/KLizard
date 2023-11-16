/*
 *
 * Copyright 2023 Kevin Hernández
 * Copyright 2012-2023 Terry Yin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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