package org.abondar.experimental.urlshortner

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.redis.testcontainers.RedisContainer
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.abondar.experimental.urlshortner.config.*
import org.abondar.experimental.urlshortner.model.ShortenRequest
import org.abondar.experimental.urlshortner.model.ShortenResponse

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@Testcontainers
class ApplicationTest {
    companion object {

        @Container
        val container = RedisContainer("redis:6.2.6")
    }

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        container.start()
    }

    @AfterEach
    fun tearDown() {
        container.stop()
    }

    @Test
    fun `test shorten endpoint`() = testApplication {
        application {
            configureApiRouting()
            configureSerialization()
            configureDI()
        }

        environment {
            config = MapApplicationConfig(
                "ktor.redis.url" to container.redisURI,
                "ktor.redis.db" to "0",
                "ktor.redis.password" to "",
            )
        }

        val client = createClient {
            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT)
                }
            }
        }

        val longUrl = "https://google.com"

        client.post("/shorten")
        {
            contentType(ContentType.Application.Json)
            setBody(ShortenRequest(longUrl))
        }
            .apply {
                assertEquals(HttpStatusCode.OK, status)

                val responseBody = bodyAsText()
                assertNotNull(responseBody)

                val response = objectMapper.readValue(responseBody, ShortenResponse::class.java)
                assertNotNull(response.url)
            }
    }


    @Test
    fun `test shorten endpoint with empty url`() = testApplication {
        application {
            configureApiRouting()
            configureSerialization()
            configureDI()
            configureStatusPages()
        }

        environment {
            config = MapApplicationConfig(
                "ktor.redis.url" to container.redisURI,
                "ktor.redis.db" to "0",
                "ktor.redis.password" to "",
            )
        }

        val client = createClient {
            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT)
                }
            }
        }

        val longUrl = ""

        client.post("/shorten")
        {
            contentType(ContentType.Application.Json)
            setBody(ShortenRequest(longUrl))
        }
            .apply {
                assertEquals(HttpStatusCode.BadRequest, status)

                val responseBody = bodyAsText()
                assertNotNull(responseBody)

                assertEquals("URL cannot be blank", responseBody)
            }
    }


    @Test
    fun `test redirect endpoint with non existing url`() = testApplication {
        application {
            configureApiRouting()
            configureSerialization()
            configureDI()
            configureStatusPages()
        }

        environment {
            config = MapApplicationConfig(
                "ktor.redis.url" to container.redisURI,
                "ktor.redis.db" to "0",
                "ktor.redis.password" to "",
            )
        }

        client.get("/test")
            .apply {
                assertEquals(HttpStatusCode.NotFound, status)

                val responseBody = bodyAsText()
                assertNotNull(responseBody)

                assertEquals("URL for redirect not found", responseBody)
            }
    }

}
