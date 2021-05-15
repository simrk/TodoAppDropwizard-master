package resources

import com.google.inject.Inject
import com.google.inject.name.Named
import entities.Representation
import entities.Todo
import entities.TodoDraft
import org.eclipse.jetty.http.HttpStatus
import repository.InMemoryTodoRepository
import repository.TodoRepository
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
class TodoResource @Inject constructor(private val repository: TodoRepository, @Named("name") private val name: String = "unknown") {


    @GET
    fun get(): String {
        return "Hello TodoList $name"
    }

    @GET
    @Path("/todos")
    fun getAllTodos(): List<Todo> {
       return repository.getAllTodos()
    }

    @GET
    @Path("/todos/{id}")
    fun getTodo(@PathParam("id") id: Int): Todo? {
        return repository.getTodo(id)
            ?: throw WebApplicationException("found no todo for id $id", HttpStatus.NOT_FOUND_404)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/todos")
    fun postTodo(todoDraft: TodoDraft): Todo {
        return repository.addTodo(todoDraft)
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/todos/{id}")
    fun putTodo(@PathParam("id") id: Int,todoDraft: TodoDraft): Representation<Boolean> {
        val updated = repository.updateTodo(id,todoDraft)
        if(updated)
        {
            return Representation<Boolean>(HttpStatus.OK_200,updated)
        }else
        {
            throw WebApplicationException("found no todo for id $id", HttpStatus.NOT_FOUND_404)
        }
    }

}