package org.abondar.experimental.urlshortner.service

import io.ktor.client.engine.callContext
import org.abondar.experimental.urlshortner.exception.UrlRequestException
import org.abondar.experimental.urlshortner.dao.UrlMappingDAO
import org.abondar.experimental.urlshortner.model.UrlMapping
import org.abondar.experimental.urlshortner.service.id.IdGenerator
import org.slf4j.LoggerFactory
import java.time.Instant

import java.util.*
import kotlin.math.abs

class UrlShortenerService(private val dao: UrlMappingDAO,
                          private val idGenerator: IdGenerator,
                          private val defaultTTL: Long)  {

    private val logger = LoggerFactory.getLogger(UrlShortenerService::class.java)


    suspend fun shortenUrl(longUrl: String): String {

        if (longUrl.isBlank()) {
            throw UrlRequestException("URL cannot be blank")
        }

        val id = idGenerator.nextId()
        val shortUrl = encodeUrl(id)

        val now = Instant.now()
        val mapping = UrlMapping(
            shortUrl,
            longUrl,
            now,
            now.plusSeconds(defaultTTL),
        )

        dao.save(mapping,ttlSeconds = defaultTTL)

        logger.info("Shortened URL: $longUrl to $shortUrl")

        return shortUrl
    }

    private fun encodeUrl(id: Long): String {
       require(id >= 0) { "ID must be non-negative" }

       var value = id
       val result = StringBuilder()

        while (value > 0){
            result.append(BASE62[((value % 62).toInt())])
            value /=62
        }

        //result is built from least significant digit - need reverse
        return result.reversed().toString()
    }

    fun getLongUrl(shortUrl: String): String {
        return dao.findLongUrl(shortUrl)
    }

    companion object {
        private const val BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    }
}