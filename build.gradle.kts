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

import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "1.9.0"
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.25.3"
}

group = "io.github.kevinah95"
version = "1.0.0" // x-release-please-version

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

publishing {
    repositories {
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/kevinah95/klizard")
            // username and password (a personal Github access token) should be specified as
            // `githubPackagesUsername` and `githubPackagesPassword` Gradle properties or alternatively
            // as `ORG_GRADLE_PROJECT_githubPackagesUsername` and `ORG_GRADLE_PROJECT_githubPackagesPassword`
            // environment variables
            credentials(PasswordCredentials::class)
        }
    }
}

mavenPublishing {
    // Configuring Maven Central
    // See: https://vanniktech.github.io/gradle-maven-publish-plugin/central/#configuring-maven-central
    publishToMavenCentral(SonatypeHost.S01, true)

    signAllPublications()

    // Configuring POM
    // See: https://vanniktech.github.io/gradle-maven-publish-plugin/central/#configuring-the-pom
    val artifactId = "klizard"

    coordinates(
        group.toString(),
        artifactId,
        version.toString()
    )

    pom {
        name.set(artifactId)
        description.set(
            "KLizard is an extensible Cyclomatic Complexity Analyzer for many programming languages including C/C++ " +
                    "(doesn''t require all the header files or Java imports). " +
                    "It also does copy-paste detection (code clone detection/code duplicate detection) and many other " +
                    "forms of static code analysis."
        )
        url.set("https://github.com/kevinah95/KLizard/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("kevinah95")
                name.set("Kevin Hernández Rostrán")
                url.set("https://github.com/kevinah95/")
                email.set("kevinah95@gmail.com")
            }
        }
        scm {
            url.set("https://github.com/kevinah95/KLizard/")
            connection.set("scm:git:git://github.com/kevinah95/KLizard.git")
            developerConnection.set("scm:git:ssh://git@github.com/kevinah95/KLizard.git")
        }
    }
}