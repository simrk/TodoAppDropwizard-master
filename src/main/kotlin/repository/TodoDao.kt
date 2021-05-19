package repository

import entities.Todo
import entities.TodoDraft
import org.skife.jdbi.v2.sqlobject.*
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper

@RegisterMapper(TodoMapper::class)
interface TodoDao
{
    @SqlQuery("select * from todo")
    fun getAllTodos(): List<Todo>

    @SqlQuery("select * from todo where id=:id")
    fun getTodo(@Bind("id") id: Int): Todo?

    @SqlUpdate("insert into todo(name,done) values(:title,:done)")
    fun addTodo(@BindBean todo: TodoDraft): Int

    @SqlUpdate("update todo set name=:title,done=:done where id=:id")
    fun getUpdateTodo(@BindBean todo:Todo): Int
}
