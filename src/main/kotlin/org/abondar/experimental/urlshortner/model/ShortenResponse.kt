package org.abondar.experimental.urlshortner.model

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class ShortenResponse(
    @param:JsonProperty(value = "short_url")
    val url: String
)
