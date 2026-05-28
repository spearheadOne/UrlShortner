package org.abondar.experimental.urlshortner.model

import java.time.Instant

data class UrlMapping(
    val shortCode: String,
    val longUrl: String,
    val createdAt: Instant,
    val expiresAt: Instant
)
