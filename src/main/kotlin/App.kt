import com.google.inject.Guice
import configuration.AppConfig
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import resources.GuiceModule
import resources.TodoResource

class App : Application<AppConfig>()
{
        companion object
        {
        @JvmStatic fun main(args : Array<String>) {
            App().run(*args)
        }
        }
    override fun run(config: AppConfig, env: Environment)
    {
        println("Configuration: ${config.name}")
        val injector = Guice.createInjector(GuiceModule(config.name))
        val instance = injector.getInstance(TodoResource::class.java)
        env.jersey().register(instance)
    }

}

