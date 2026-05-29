package org.abondar.experimental.urlshortner.model

import kotlinx.serialization.Serializable

@Serializable
data class ShortenRequest(

    val url: String
)