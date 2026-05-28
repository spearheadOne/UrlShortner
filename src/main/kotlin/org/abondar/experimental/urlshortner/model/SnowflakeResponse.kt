package org.abondar.experimental.urlshortner.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SnowflakeResponse(
    @field:JsonProperty(value = "snowflake_id")
    val snowflakeId: Long
)
