package resources

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.name.Named
import repository.*

class GuiceModule(
    private val name: String,
    private val type: String
) : AbstractModule() {

    override fun configure() {
        val dbInstance = DBIClientBuilder().build()
        bind(TodoDao::class.java).toInstance(dbInstance.onDemand(TodoDao::class.java))
        bind(TodoRepository::class.java).to(SqlDatabaseTodoRepository::class.java)
//        bind(String::class.java).toInstance(name)
//        bind(String::class.java).annotatedWith(Names.named("name")).toInstance(name)
//        bind(String::class.java).toInstance(type)
    }

    @Named("name")
    @Provides
    fun getName(): String {
        return name;
    }

}