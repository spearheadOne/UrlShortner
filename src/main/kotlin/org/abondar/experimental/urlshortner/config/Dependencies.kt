package org.abondar.experimental.urlshortner.config

import com.datastax.oss.driver.api.core.CqlSession
import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.*
import org.abondar.experimental.urlshortner.dao.UrlMappingDAO
import org.abondar.experimental.urlshortner.service.UrlShortenerService
import org.abondar.experimental.urlshortner.service.id.IdGenerator
import org.abondar.experimental.urlshortner.service.id.LocalIdGenerator
import org.abondar.experimental.urlshortner.service.id.ResilientIdGenerator
import org.abondar.experimental.urlshortner.service.id.SnowflakeIdGenerator
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.singleton
import redis.clients.jedis.Jedis

fun Application.configureDI() {

    val cassandraSession = configureCassandra()
    val redisClient = configureRedis()

    val defaultTTL = environment.config.property("ktor.urlshortner.defaultTtlSeconds").getString().toLong()
    val snowflakeUrl = environment.config.property("ktor.urlshortner.snowflake.url").getString()
    val snowflakeRetryCount = environment.config.property("ktor.urlshortner.snowflake.retryCount").getString().toInt()

    di {

        bind<CqlSession>() with singleton {
            cassandraSession
        }

        bind<Jedis>() with singleton {
            redisClient
        }

        bind<HttpClient>() with singleton {
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    jackson()
                }
            }
        }

        bind<SnowflakeIdGenerator>() with singleton {
            SnowflakeIdGenerator(instance(), snowflakeUrl)
        }

        bind<LocalIdGenerator>() with singleton {
            LocalIdGenerator()
        }

        bind<IdGenerator>() with singleton {
            ResilientIdGenerator(instance<SnowflakeIdGenerator>(), instance<LocalIdGenerator>(), snowflakeRetryCount)
        }

        bind<UrlMappingDAO>() with singleton {
            UrlMappingDAO(instance(), instance())
        }

        bind<UrlShortenerService>() with singleton {
            UrlShortenerService(instance(), instance(), defaultTTL)
        }

    }


}