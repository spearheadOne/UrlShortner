package org.abondar.experimental.urlshortner.service.id

interface IdGenerator {

    suspend fun nextId(): Long

}