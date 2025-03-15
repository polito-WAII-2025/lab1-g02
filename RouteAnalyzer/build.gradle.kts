plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "1.4.20"
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
}

tasks.test {
    useJUnitPlatform()
}

application{
    mainClass = "org.example.MainKt"
}
