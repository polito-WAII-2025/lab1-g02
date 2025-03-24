plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "1.4.20"
    id("com.gradleup.shadow") version "8.3.6"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.uber:h3:4.1.1")
    implementation("com.charleskorn.kaml:kaml:0.72.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("com.github.java-json-tools:json-schema-validator:2.2.14")


}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.example.MainKt"
    }
}


tasks.test {
    useJUnitPlatform()
}

application{
    mainClass = "org.example.MainKt"
}
