package org.abondar.experimental.urlshortner.config

import com.datastax.oss.driver.api.core.CqlSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import redis.clients.jedis.Jedis

fun Application.configureMetrics() {

    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val redisClient by closestDI().instance<Jedis>()
    val cassandraSession by closestDI().instance<CqlSession>()

    install(MicrometerMetrics) {
        this.registry = registry
    }

    routing {
        routing {
            get("/metrics") {
                call.respondText(registry.scrape())
            }

            get("/health") {

                val redisUp = runCatching {
                    redisClient.ping() == "PONG"
                }.getOrDefault(false)

                val cassandraUp = runCatching {
                    cassandraSession.execute("SELECT now() FROM system.local").one() !=null
                }.getOrDefault(false)

                val status = if (redisUp && cassandraUp) "UP" else "DOWN"
                val response = mapOf(
                    "status" to status,
                    "components" to mapOf(
                        "redis" to mapOf(
                            "status" to if (redisUp) "UP" else "DOWN"
                        ),
                        "cassandra" to mapOf(
                            "status" to if (cassandraUp) "UP" else "DOWN"
                        )
                    )
                )


                call.respondText(
                    response.toString())
            }
        }

    }
}