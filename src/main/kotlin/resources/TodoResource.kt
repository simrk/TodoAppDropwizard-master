package resources

import com.google.inject.Inject
import com.google.inject.name.Named
import entities.Representation
import entities.Todo
import entities.TodoDraft
import org.eclipse.jetty.http.HttpStatus
import repository.TodoRepository
import javax.ws.rs.*
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended
import javax.ws.rs.core.MediaType


@Path("/api/v1/todos")
@Produces(MediaType.APPLICATION_JSON)
class TodoResource @Inject constructor(private val todoRepository: TodoRepository, @Named("name")private val name: String = "unknown", private val type: String = "unknown")
{

    @Path("/ok")
    @GET
    fun get(@Suspended asyncResponse: AsyncResponse): String {
          return "Hello TodoList $name and $type"
    }

    @GET
    @Path("/getAll")
    @Consumes(MediaType.APPLICATION_JSON)
    fun getAllTodos(): List<Todo> {
        return todoRepository.getAllTodos()
    }

    @GET
    @Path("/todos/{id}")
    fun getTodo(@PathParam("id") id: Int): Todo? {
        return todoRepository.getTodo(id)
            ?: throw WebApplicationException("found no todo for id $id", HttpStatus.NOT_FOUND_404)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/todos")
    fun postTodo(todoDraft: TodoDraft): Boolean {
        return todoRepository.addTodo(todoDraft)
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/todos/{id}")
    fun putTodo(@PathParam("id") id: Int,todoDraft: TodoDraft): Representation<Int> {
        val updated = todoRepository.updateTodo(id,todoDraft)
        if(updated!=0)
        {
            return Representation<Int>(HttpStatus.OK_200,updated)
        }else
        {
            throw WebApplicationException("found no todo for id $id", HttpStatus.NOT_FOUND_404)
        }
    }

}