package entities

object TodoResponses
{
    data class SuccessResponse(
        val data: Any?,
        val code: Int = 200,
        val message: String = "SUCCESS"
    )

    data class FailureResponse(
        val data: Any?,
        val code: Int = 400,
        val message: String = "FAILURE"
    )
}