package org.abondar.experimental.urlshortner.service.id

class ResilientIdGenerator(
    private val snowflakeIdGenerator: IdGenerator,
    private val localIdGenerator: IdGenerator,
    private val retryCount: Int
): IdGenerator {
    override suspend fun nextId(): Long {
         repeat(retryCount) {
             try {
                 return snowflakeIdGenerator.nextId()
             } catch (_: Exception) {
                 Thread.sleep(100)
             }
         }
         return localIdGenerator.nextId()
    }
}