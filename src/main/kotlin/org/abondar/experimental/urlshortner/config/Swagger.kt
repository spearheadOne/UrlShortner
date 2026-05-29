package org.abondar.experimental.urlshortner.config

import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureSwagger() {
    routing {
        swaggerUI(path = "/swaggerUI") {
            info = OpenApiInfo("Shortener API", "1.0.0", "Tiny url shortener")
        }
    }
}