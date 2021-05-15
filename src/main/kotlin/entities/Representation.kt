package entities

import com.fasterxml.jackson.annotation.JsonProperty

data class Representation<T>(
    @JsonProperty("code")
    val code: Int,
    @JsonProperty("data")
    val data: T
    )