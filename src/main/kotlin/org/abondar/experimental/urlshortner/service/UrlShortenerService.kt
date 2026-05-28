package org.abondar.experimental.urlshortner.service

import org.abondar.experimental.urlshortner.exception.UrlRequestException
import org.abondar.experimental.urlshortner.dao.UrlMappingDAO
import org.abondar.experimental.urlshortner.model.UrlMapping
import org.slf4j.LoggerFactory
import java.time.Instant

import java.util.*
import kotlin.math.abs

class UrlShortenerService(private val dao: UrlMappingDAO, private val defaultTTL: Long)  {

    private val logger = LoggerFactory.getLogger(UrlShortenerService::class.java)


    fun shortenUrl(longUrl: String): String {

        if (longUrl.isBlank()) {
            throw UrlRequestException("URL cannot be blank")
        }

        val shortUrl = encodeUrl()

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

    private fun encodeUrl(): String {
       var id = UUID.randomUUID().mostSignificantBits
       val shortUrl = StringBuilder()

        if (id == 0L){
            id = UUID.randomUUID().leastSignificantBits
        }

        id = abs(id)

        while (id >0){
            shortUrl.append(BASE62[((id % 62).toInt())])
            id /=62
        }

        //result is built from least significant digit - need reverse
        return shortUrl.reversed().toString()
    }

    fun getLongUrl(shortUrl: String): String {
        return dao.findLongUrl(shortUrl)
    }

    companion object {
        private const val BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    }
}