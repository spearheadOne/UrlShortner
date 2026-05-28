package org.abondar.experimental.urlshortner.service.id

import java.util.UUID

class LocalIdGenerator : IdGenerator {
    override suspend fun nextId(): Long {
       return UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
    }
}