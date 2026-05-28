package org.abondar.experimental.urlshortner.service

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.abondar.experimental.urlshortner.dao.UrlMappingDAO
import org.abondar.experimental.urlshortner.exception.UrlNotFoundException
import org.abondar.experimental.urlshortner.exception.UrlRequestException
import org.abondar.experimental.urlshortner.service.id.LocalIdGenerator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.singleton


class UrlShortenerServiceTest {

    private lateinit var urlMappingDAO: UrlMappingDAO
    private val idGenerator = LocalIdGenerator()
    private lateinit var kodein: DI
    private lateinit var urlShortenerService: UrlShortenerService


    @BeforeEach
    fun setup() {
        urlMappingDAO = mockk(relaxed = true)

        kodein = DI {
            bind<UrlMappingDAO>() with singleton { urlMappingDAO }
            bind<UrlShortenerService>() with singleton { UrlShortenerService(instance(), idGenerator, 100) }
        }


        urlShortenerService = kodein.direct.instance()
    }


    @AfterEach
    fun tearDown() {
        clearMocks(urlMappingDAO)
    }

    @Test
    fun `test url shortener `() = runBlocking {
        val longUrl = "http://www.test.com"

        every { urlMappingDAO.save(any(), any()) } returns Unit


        val result = urlShortenerService.shortenUrl(longUrl)

        assertNotNull(result)
        assertFalse(result.isEmpty())

        verify { urlMappingDAO.save(any(), any()) }


    }

    @Test
    fun `test url shortener empty url`() = runBlocking {
        val longUrl = ""

        val exception = assertThrows(UrlRequestException::class.java) {
            runBlocking {
                urlShortenerService.shortenUrl(longUrl)
            }

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