package entities

import com.fasterxml.jackson.annotation.JsonProperty

data class TodoDraft(
    @JsonProperty("title")
    val title: String,
    @JsonProperty("done")
    val done: Boolean,
)