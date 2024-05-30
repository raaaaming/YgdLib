plugins {
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("maven-publish")
}

group = "org.raming"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    api("org.jetbrains.kotlin", "kotlin-stdlib", "1.8.0")
    api("org.jetbrains.kotlin", "kotlin-reflect", "1.8.0")
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.6.4")

    api("org.jetbrains.exposed", "exposed-core", "0.41.1")
    api("org.jetbrains.exposed", "exposed-dao", "0.41.1")
    api("org.jetbrains.exposed", "exposed-jdbc", "0.41.1")
    api("org.jetbrains.exposed", "exposed-java-time", "0.41.1")

    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = rootProject.version.toString()

            from(components["java"])
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
    jar {
        destinationDirectory.set(file("C:\\Users\\raaaaming\\Desktop\\프로그래밍\\TestServer\\plugins"))
    }
}
