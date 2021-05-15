package repository

import entities.Todo
import entities.TodoDraft

interface TodoRepository
{
    fun getAllTodos(): List<Todo>

    fun getTodo(id:Int): Todo?

    fun addTodo(draft: TodoDraft):Todo

    fun updateTodo(id:Int,draft: TodoDraft):Boolean

}