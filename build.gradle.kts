plugins {
    kotlin("jvm") version "2.1.21"
    id("maven-publish")
}

group = "com.github.AxieFeat"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["kotlin"])

            groupId = "com.github.AxieFeat"
            artifactId = project.name

            pom {
                name.set(project.properties["POM_NAME"].toString())
                description.set(project.description)
            }
        }
    }
}