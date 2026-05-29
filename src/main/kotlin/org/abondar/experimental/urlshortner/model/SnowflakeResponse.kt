package org.abondar.experimental.urlshortner.model

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class SnowflakeResponse(
    @field:JsonProperty(value = "snowflake_id")
    val snowflakeId: Long
)
