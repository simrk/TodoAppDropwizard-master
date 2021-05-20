package entities

object TodoRequests
{
    data class AddTodo(
        val description: String,
        val userId: String
    )

    data class EditTodo(
        val todoId: Int,
        val description: String
    )

    data class ChangeStatus(
        val todoId: Int,
        val status: Status
    )

}