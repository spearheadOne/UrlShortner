package org.abondar.experimental.urlshortner.dao

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.Row
import org.abondar.experimental.urlshortner.exception.UrlNotFoundException
import org.abondar.experimental.urlshortner.model.UrlMapping
import redis.clients.jedis.Jedis
import java.time.Instant

class UrlMappingDAO(private val redisClient: Jedis, private val cassandraSession: CqlSession) {

    fun save(mapping: UrlMapping, ttlSeconds: Long) {
        require(ttlSeconds > 0) { "ttl must be positive" }

        cassandraSession.execute(
            """
                INSERT INTO urlshortener.url_mapping (short_code, long_url, created_at, expires_at) 
                    VALUES (?, ?, ?, ?) 
                    USING TTL ?
            """.trimIndent(), mapping.shortCode, mapping.longUrl,
            mapping.createdAt, mapping.expiresAt, ttlSeconds.toInt()
        )


        redisClient.setex(mapping.shortCode, ttlSeconds, mapping.longUrl)
    }

    fun findLongUrl(shortUrl: String): String {
        redisClient.get(shortUrl)
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        val row = cassandraSession.execute(
            """
        SELECT long_url
        FROM urlshortener.url_mapping
        WHERE short_code = ?
        """.trimIndent(), shortUrl
        ).one() ?: throw UrlNotFoundException("URL for redirect not found")


        val mapping = mapRow(row)
        val ttlSeconds = mapping.expiresAt.epochSecond.minus(Instant.now().epochSecond)

        if (ttlSeconds > 0) {
            redisClient.setex(shortUrl, ttlSeconds, mapping.longUrl)
        }

        return mapping.longUrl

    }

    private fun mapRow(row: Row): UrlMapping {
        return UrlMapping(
            shortCode = row.getString("short_code")
                ?: throw UrlNotFoundException("Short code not found"),

            longUrl = row.getString("long_url")
                ?: throw UrlNotFoundException("Long URL not found"),

            createdAt = row.getInstant("created_at")
                ?: throw UrlNotFoundException("Created at not found"),

            expiresAt = row.getInstant("expires_at")
                ?: throw UrlNotFoundException("Expires at not found")
        )
    }


}