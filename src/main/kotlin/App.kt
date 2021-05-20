import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.Guice
import configuration.AppConfig
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import resources.GuiceModule
import resources.SampleResource
import resources.TodoResource

class App : Application<AppConfig>() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            App().run(*args)
        }
    }

    override fun run(config: AppConfig, env: Environment)
    {
        println("Configuration: ${config.name}")
        val injector = Guice.createInjector(GuiceModule(config.name, config.type))
        val instance1 = injector.getInstance(SampleResource::class.java)
        val instance2 = injector.getInstance(TodoResource::class.java)
        env.jersey().register(instance1)
        env.jersey().register(instance2)
    }

    override fun initialize(bootstrap: Bootstrap<AppConfig>) {
        bootstrap.objectMapper.registerKotlinModule()
    }
}

