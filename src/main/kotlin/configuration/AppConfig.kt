package configuration


import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration
import io.dropwizard.db.DataSourceFactory

class AppConfig(val name: String = "unknown",val type: String="unknown"): Configuration()