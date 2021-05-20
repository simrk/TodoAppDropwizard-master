package repository

import entities.Status
import entities.Todo
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper
import java.sql.ResultSet

//class TodoMapper : ResultSetMapper<Todo>
//{
//    override fun map(index: Int, resultSet: ResultSet?, statementContext: StatementContext?): Todo
//    {
//            return Todo(
//                resultSet!!.getInt("todo_id"),
//                resultSet!!.getString("description"),
//                Status.values()[resultSet!!.getInt("status")],
//                resultSet!!.getString("user_id")
//            )
//    }
//
//}