package resources

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.name.Names
import repository.InMemoryTodoRepository
import repository.InMemoryTodoRepository2
import repository.TodoRepository
import sun.misc.MessageUtils.out
import java.lang.System.out
import java.time.LocalTime

class GuiceModule(private val name: String) : AbstractModule()
{

    override fun configure()
    {
        bind(String::class.java).annotatedWith(Names.named("name")).toInstance(name)
    }

    @Provides
    fun get(): TodoRepository
    {
        //TODO: based on this configuration , select the proper class type to be injected
        return if(name=="kotlin") {
            InMemoryTodoRepository()
        }else {
            InMemoryTodoRepository2()
        }
    }

}