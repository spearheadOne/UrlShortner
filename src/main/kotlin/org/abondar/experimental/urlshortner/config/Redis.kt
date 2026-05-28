package org.abondar.experimental.urlshortner.config

import io.ktor.server.application.Application
import redis.clients.jedis.Jedis


fun Application.configureRedis(): Jedis {
    val redisUrl = environment.config.property("ktor.redis.url").getString()
    val redisDb = environment.config.property("ktor.redis.db").getString().toInt()
    val redisPassword = environment.config.property("ktor.redis.password").getString()

    return Jedis(redisUrl).apply {
        if (redisPassword.isNotBlank()) {
            auth(redisPassword)
        }

        select(redisDb)
    }

}