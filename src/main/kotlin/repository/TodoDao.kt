package repository

import entities.Status
import entities.Todo
import org.skife.jdbi.v2.sqlobject.*
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper

@RegisterMapper(TodoMapper::class)
interface TodoDao {
    companion object {
        private const val TABLE_NAME = "todo"
        private const val INSERT = "insert into $TABLE_NAME"
        private const val UPDATE = "update $TABLE_NAME"
    }

    @SqlQuery("select * from $TABLE_NAME where user_id = :userId LIMIT :limit OFFSET :offset")
    fun getAllTodosForUserId(
        @Bind("userId") userId: String,
        @Bind("limit") limit: Int,
        @Bind("offset") offset: Int): List<Todo>?


    @SqlQuery("select * from $TABLE_NAME where user_id = :userId and status=:status LIMIT :limit OFFSET :offset")
    fun getAllTodosForUserId(
        @Bind("userId") userId: String,
        @Bind("status") status: Status,
        @Bind("limit") limit: Int,
        @Bind("offset") offset: Int): List<Todo>?

    @SqlQuery("select * from $TABLE_NAME where todo_id=:todoId")
    fun getTodo(@Bind("todoId") todoId: Int): Todo?

    @SqlUpdate("$INSERT (description,status,user_id) values (:description,:status,:userId)")
    fun addTodo(@BindBean todo: Todo): Int

    @SqlUpdate("$UPDATE set description=:description where todo_id=:todoId")
    fun updateTodo(@Bind("todoId") todoId: Int,
                   @Bind("description")description: String): Int

    @SqlUpdate("$UPDATE set status= :status where todo_id= :todoId")
    fun updateTodo(@Bind("todoId") todoId: Int,
                   @Bind("status") status: Status): Int

}

class TodoMapper: KotlinMapper<Todo>(Todo::class.java)