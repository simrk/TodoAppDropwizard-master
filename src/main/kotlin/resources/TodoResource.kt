package resources

import com.google.inject.Inject
import com.google.inject.name.Named
import entities.*
import repository.TodoRepository
import javax.ws.rs.*
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended
import javax.ws.rs.core.MediaType


@Path("/api/v1/todos")
@Produces(MediaType.APPLICATION_JSON)
class TodoResource @Inject constructor(private val todoRepository: TodoRepository,
                                       @Named("name") private val name: String,
                                       private val type: String) {

    @Path("/ok")
    @GET
    fun get(@Suspended asyncResponse: AsyncResponse) {
        return asyncResponse.with {
            TodoResponses.SuccessResponse("OK : Hello TodoList $name and $type ")
        }
    }

    @GET
    @Path("/getAll")
    @Consumes(MediaType.APPLICATION_JSON)
    fun getAllTodos(
        @QueryParam("userId") userId: String,
        @QueryParam("page") page: Int,
        @QueryParam("count") @DefaultValue("10") count: Int,
        @Suspended asyncResponse: AsyncResponse
    ) {
        return try {
            asyncResponse.with {
                TodoResponses.SuccessResponse(todoRepository.getAllTodos(userId, page, count))
            }
        } catch (e: Exception) {
            asyncResponse.with {
                TodoResponses.FailureResponse(e)
            }
        }

    }

    @GET
    @Path("/get/{id}")
    fun getTodo(@PathParam("id") id: Int, @Suspended asyncResponse: AsyncResponse) {
        try {
            return asyncResponse.with {
                TodoResponses.SuccessResponse(todoRepository.getTodo(id))
            }
        } catch (ex: Exception) {
            asyncResponse.with {
                TodoResponses.FailureResponse(ex)
            }
        }
    }

    @Path("/getByStatus")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    fun getByStatus(@QueryParam("userId") userId: String,
                    @QueryParam("status") status: Status,
                    @QueryParam("page") page: Int,
                    @QueryParam("count") @DefaultValue("10") count: Int,
                    @Suspended asyncResponse: AsyncResponse) {
        return try {
            asyncResponse.with {
                TodoResponses.SuccessResponse(
                    todoRepository.getTodoByStatus(userId, status, page, count)
                )
            }
        } catch (e: Exception) {
            asyncResponse.with {
                TodoResponses.FailureResponse(e)
            }
        }
    }

    @Path("/add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun addTodo(request: TodoRequests.AddTodo, @Suspended asyncResponse: AsyncResponse) {
        return try {
            asyncResponse.with {
                TodoResponses.SuccessResponse(todoRepository.addTodo(request.description, request.userId) == 1)
            }
        } catch (e: Exception) {
            asyncResponse.with {
                TodoResponses.FailureResponse(e)
            }
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/edit")
    fun editTodo(request: TodoRequests.EditTodo, @Suspended asyncResponse: AsyncResponse) {
        return try {
            asyncResponse.with {
                TodoResponses.SuccessResponse(
                    todoRepository.editTodo(request.todoId, request.description) == 1
                )
            }
        } catch (e: Exception) {
            asyncResponse.with {
                TodoResponses.FailureResponse(e)
            }
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/changeStatus")
    fun editTodo(request: TodoRequests.ChangeStatus, @Suspended asyncResponse: AsyncResponse) {
        return try {
            asyncResponse.with {
                TodoResponses.SuccessResponse(
                    todoRepository.changeStatus(request.todoId, request.status) == 1
                )
            }
        } catch (e: Exception) {
            asyncResponse.with {
                TodoResponses.FailureResponse(e)
            }
        }
    }


}