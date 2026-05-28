val kotlin_version: String by project
val logback_version: String by project
val swagger_version: String by project
val jedis_version: String by project
val kodein_version: String by project
val junit_version: String by project
val mockk_version: String by project
val testcontainers_version: String by project
val cassandra_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.12"
    id("com.google.cloud.tools.jib") version "3.4.3"
}

group = "org.abondar.experimental.urlshortner"
version = "1.0"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-client-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-jackson-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-config-yaml-jvm")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-client-cio-jvm:")

    implementation("io.github.smiley4:ktor-swagger-ui:$swagger_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:$kodein_version")

    implementation("redis.clients:jedis:$jedis_version")

    implementation("com.datastax.oss:java-driver-core:$cassandra_version")
    implementation("com.datastax.oss:java-driver-query-builder:$cassandra_version")


    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("io.mockk:mockk:$mockk_version")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")

    testImplementation("org.testcontainers:testcontainers:$testcontainers_version")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainers_version")
    testImplementation("com.redis:testcontainers-redis:2.2.2")

}


tasks {
    register<Test>("integrationTest") {
        useJUnitPlatform {
            include("**/ApplicationTest.class")
        }
    }

    named<Test>("test") {
        useJUnitPlatform {
            exclude("**/ApplicationTest.class")
        }

    }
}
var registry = System.getenv("DOCKER_REGISTRY")

jib {

    from {
        image = "eclipse-temurin:21-jre"
        platforms {
//            platform {
//                architecture = "amd64"
//                os = "linux"
//            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }

    }

    to {
        image = "$registry/urlshortner:$version"
        auth {
            username = System.getenv("DOCKER_USERNAME")
            password = System.getenv("DOCKER_PWD")
        }
    }

    container {
        mainClass = mainClass
        ports = listOf("8080")
        args = listOf("-config=application-docker.yaml")
    }

    extraDirectories {
        paths {
            path {
                setFrom("src/main/resources/docker")
            }
        }
    }

}