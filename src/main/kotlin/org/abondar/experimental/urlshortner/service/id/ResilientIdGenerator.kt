package org.abondar.experimental.urlshortner.service.id

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.milliseconds

class ResilientIdGenerator(
    private val snowflakeIdGenerator: IdGenerator,
    private val localIdGenerator: IdGenerator,
    private val retryCount: Int
) : IdGenerator {

    private val logger = LoggerFactory.getLogger(ResilientIdGenerator::class.java)

    override suspend fun nextId(): Long {

        repeat(retryCount) { attempt ->
            try {
                val id = snowflakeIdGenerator.nextId()
                if (id > 0) return id

                logger.warn("Snowflake returned invalid ID {} on attempt {}", id, attempt + 1)
            } catch (ex: Exception) {
                logger.warn("Snowflake ID request failed on attempt {}", attempt + 1, ex)
            }

            delay(100.milliseconds)
        }
        return localIdGenerator.nextId()
    }
}