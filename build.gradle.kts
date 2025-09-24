val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("io.ktor.plugin") version "3.3.0"
    id("com.google.cloud.tools.jib") version "3.4.2"
}

group = "org.jetbrains.edu.junie2middie"
version = "0.0.1"

application {
    //mainClass = "io.ktor.server.netty.EngineMain"
    mainClass = "org.jetbrains.edu.junie2middie.ApplicationKt"
}

jib {
    from {
        image = "eclipse-temurin:21-jre"
    }
    to {
        image = "j2m-backend:${project.version}"
    }
    container {
        mainClass = "io.ktor.server.netty.EngineMain"
        ports = listOf("8080")
        jvmFlags = listOf("-Djava.security.egd=file:/dev/./urandom")
        environment = mapOf(
            "JAVA_TOOL_OPTIONS" to "-XX:+UseContainerSupport"
        )
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-config-yaml")

    // JSON serialization
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    // Database
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.xerial:sqlite-jdbc:3.47.0.0")

    // CLI parsing
    implementation("com.github.ajalt.clikt:clikt:4.3.0")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
