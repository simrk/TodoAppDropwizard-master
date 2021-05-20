package repository

import entities.Status
import entities.Todo

interface TodoRepository
{
    fun getAllTodos(userId: String,page: Int,count: Int): List<Todo>?

    fun getTodo(todoId: Int): Todo?

    fun getTodoByStatus(userId: String,status: Status,page: Int,count: Int): List<Todo>?

    fun addTodo(description: String,userId: String):Int

    fun editTodo(todoId:Int, description: String):Int

    fun changeStatus(todoId: Int,status: Status): Int
}