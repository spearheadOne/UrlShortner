package org.abondar.experimental.urlshortner.service

import com.datastax.oss.driver.api.core.CqlSession
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.abondar.experimental.urlshortner.dao.UrlMappingDAO
import org.abondar.experimental.urlshortner.exception.UrlNotFoundException
import org.abondar.experimental.urlshortner.exception.UrlRequestException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.*
import redis.clients.jedis.Jedis


class UrlShortenerServiceTest {

    private lateinit var redisClient: Jedis
    private lateinit var cassandraSession: CqlSession
    private lateinit var urlMappingDAO: UrlMappingDAO
    private lateinit var kodein: DI
    private lateinit var urlShortenerService: UrlShortenerService


    @BeforeEach
    fun setup() {
        redisClient = mockk(relaxed = true)
        cassandraSession = mockk(relaxed = true)
        urlMappingDAO = mockk(relaxed = true)

        kodein = DI {
            bind<Jedis>() with singleton { redisClient }
            bind<CqlSession>() with singleton { cassandraSession }
            bind<UrlMappingDAO>() with singleton { urlMappingDAO }
            bind<UrlShortenerService>() with singleton { UrlShortenerService(instance(), 100) }
        }


        urlShortenerService = kodein.direct.instance()
    }


    @AfterEach
    fun tearDown() {
        clearMocks(redisClient, urlMappingDAO)
    }

    @Test
    fun `test url shortener `() {
        val longUrl = "http://www.test.com"

        every { urlMappingDAO.save(any(), any()) } returns Unit


        val result = urlShortenerService.shortenUrl(longUrl)

        assertNotNull(result)
        assertFalse(result.isEmpty())

        verify { urlMappingDAO.save(any(), any()) }


    }

    @Test
    fun `test url shortener empty url`() {
        val longUrl = ""

        val exception = assertThrows(UrlRequestException::class.java) {
            urlShortenerService.shortenUrl(longUrl)
        }

        assertEquals("URL cannot be blank", exception.message)

    }

    @Test
    fun `test find long url `() {
        val longUrl = "http://www.test.com"
        val shortUrl = "sdd"

        every { urlMappingDAO.findLongUrl(shortUrl) } returns longUrl
        val result = urlShortenerService.getLongUrl(shortUrl)

        assertEquals(longUrl, result)

    }


    @Test
    fun `test long url not found `() {
        val shortUrl = "sdd"

        every { urlMappingDAO.findLongUrl(shortUrl) } throws UrlNotFoundException("URL for redirect not found")

        val exception = assertThrows(UrlNotFoundException::class.java) {
            urlShortenerService.getLongUrl(shortUrl)
        }

        assertEquals("URL for redirect not found", exception.message)

    }

}