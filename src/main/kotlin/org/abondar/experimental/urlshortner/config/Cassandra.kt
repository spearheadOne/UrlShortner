package org.abondar.experimental.urlshortner.config


import com.datastax.oss.driver.api.core.CqlSession
import io.ktor.server.application.Application
import java.net.InetSocketAddress

fun Application.configureCassandra(): CqlSession {

    val contactPoints = environment.config
        .property("ktor.cassandra.contactPoints")
        .getString()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val datacenter = environment.config
        .property("ktor.cassandra.localDatacenter")
        .getString()

    val builder = CqlSession.builder()
        .withLocalDatacenter(datacenter)

    contactPoints.forEach { contactPoint ->
        val host = contactPoint.substringBefore(":")
        val port = contactPoint.substringAfter(":").toInt()
        builder.addContactPoint(InetSocketAddress(host, port))
    }

    val session = builder.build()

    configureCassandraSchema(session)

    return session
}


fun configureCassandraSchema(session: CqlSession) {
    session.execute(
        """
        CREATE KEYSPACE IF NOT EXISTS urlshortener
        WITH replication = {
          'class': 'SimpleStrategy',
          'replication_factor': 1
        }
        """.trimIndent()
    )

    session.execute(
        """
        CREATE TABLE IF NOT EXISTS urlshortener.url_mapping (
          short_code text PRIMARY KEY,
          long_url text,
          created_at timestamp,
          expires_at timestamp
        )
        """.trimIndent()
    )
}