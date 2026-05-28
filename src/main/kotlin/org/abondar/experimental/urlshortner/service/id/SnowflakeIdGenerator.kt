package org.abondar.experimental.urlshortner.service.id

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.abondar.experimental.urlshortner.model.SnowflakeResponse
import org.slf4j.LoggerFactory

class SnowflakeIdGenerator(
    private val client: HttpClient,
    private val snowflakeUrl: String,
) : IdGenerator {

    private val logger = LoggerFactory.getLogger(SnowflakeIdGenerator::class.java)

    override suspend fun nextId(): Long {

        val response = client.get(snowflakeUrl)
            .body<SnowflakeResponse>()

        logger.debug("Received snowflake ID {}", response.snowflakeId)

        return response.snowflakeId
    }
}