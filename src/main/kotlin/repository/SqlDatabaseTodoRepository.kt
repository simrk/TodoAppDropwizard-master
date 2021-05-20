package repository

import com.google.inject.Inject
import com.google.inject.Singleton
import entities.Status
import entities.Todo

@Singleton
class SqlDatabaseTodoRepository @Inject constructor(private val todoDao: TodoDao) : TodoRepository {

    override fun getAllTodos(userId: String,page: Int, count: Int): List<Todo>?
    {
        val offset: Int = (page-1) * count
        return todoDao.getAllTodosForUserId(userId,count,offset);
    }

    override fun getTodo(todoId: Int): Todo? {
        return todoDao.getTodo(todoId)
    }

    override fun getTodoByStatus(userId: String, status: Status, page: Int, count: Int): List<Todo>? {
        val offset: Int = (page - 1)* count
        return todoDao.getAllTodosForUserId(userId,status,count,offset)
    }

    override fun addTodo(description: String, userId: String): Int {
         return todoDao.addTodo(Todo(0,description,Status.PENDING,userId))
    }

    override fun editTodo(todoId: Int, description: String): Int {
          return todoDao.updateTodo(todoId,description)
    }

    override fun changeStatus(todoId: Int, status: Status): Int {
         return todoDao.updateTodo(todoId, status)
    }

}