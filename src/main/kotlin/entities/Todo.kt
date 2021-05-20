package entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

enum class Status{
    PENDING,
    DONE
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Todo(
    val todoId: Int,
    val description: String,
    val status: Status,
    val userId: String
)