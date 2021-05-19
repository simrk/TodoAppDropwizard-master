package repository

import entities.Todo
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper
import java.sql.ResultSet

class TodoMapper : ResultSetMapper<Todo>
{
    override fun map(index: Int, resultSet: ResultSet?, statementContext: StatementContext?): Todo?
    {
        if (resultSet != null) {
            return Todo(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getBoolean("done")
            )
        }
        return null;
    }

}