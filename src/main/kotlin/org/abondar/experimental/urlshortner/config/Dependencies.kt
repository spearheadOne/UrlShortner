package org.abondar.experimental.urlshortner.config

import com.datastax.oss.driver.api.core.CqlSession
import io.ktor.server.application.*
import org.abondar.experimental.urlshortner.dao.UrlMappingDAO
import org.abondar.experimental.urlshortner.service.UrlShortenerService
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.singleton
import redis.clients.jedis.Jedis

fun Application.configureDI() {

    val cassandraSession = configureCassandra()
    val redisClient = configureRedis()

    val defaultTTL = environment.config.property("ktor.urlshortner.defaultTtlSeconds").getString().toLong()

    di {

        bind<CqlSession>() with singleton {
            cassandraSession
        }

        bind<Jedis>() with singleton {
            redisClient
        }

        bind<UrlMappingDAO>() with singleton {
            UrlMappingDAO(instance(),instance())
        }

        bind<UrlShortenerService>() with singleton {
            UrlShortenerService(instance(), defaultTTL)
        }

    }


}