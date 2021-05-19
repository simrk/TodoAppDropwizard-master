package repository

import com.google.inject.Inject
import entities.Todo
import entities.TodoDraft

class SqlDatabaseTodoRepository @Inject constructor(private val todoDao: TodoDao) : TodoRepository {

    override fun getAllTodos(): List<Todo> {
        return todoDao.getAllTodos();
    }

    override fun getTodo(id: Int): Todo? {
        return todoDao.getTodo(id)
    }

    override fun addTodo(draft: TodoDraft): Boolean {
        val rowsEffected = todoDao.addTodo(draft)
        return rowsEffected == 1;
    }

    override fun updateTodo(id: Int, draft: TodoDraft): Int
    {
       val todo = Todo(
            id=id,
            title = draft.title,
            done = draft.done
        )
        return todoDao.getUpdateTodo(todo)
    }

}