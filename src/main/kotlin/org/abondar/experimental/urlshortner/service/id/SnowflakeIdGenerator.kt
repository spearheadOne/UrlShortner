package org.abondar.experimental.urlshortner.service.id

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.abondar.experimental.urlshortner.model.SnowflakeResponse

class SnowflakeIdGenerator(
    private val client: HttpClient,
    private val snowflakeUrl: String,
) : IdGenerator {

    override suspend fun nextId(): Long {

        return client.get(snowflakeUrl)
            .body<SnowflakeResponse>()
            .snowflakeId
    }
}