package repository

import com.codahale.metrics.MetricRegistry
import io.dropwizard.db.DataSourceFactory
import io.dropwizard.db.PooledDataSourceFactory
import io.dropwizard.jdbi.DBIFactory
import org.skife.jdbi.v2.DBI

class DBIClientBuilder
{
    fun build(): DBI
    {
        return getDBI(this)
    }

    @Synchronized
    private fun getDBI(builder: DBIClientBuilder) : DBI
    {
        val dataSourceFactory = getDataSourceFactory(builder)
        val managedDataSource = dataSourceFactory.build(MetricRegistry(),"/id/db/user")
        return object : DBIFactory() {
            fun build() = DBI(managedDataSource).also{
                configure(it,dataSourceFactory)
            }
        }.build()
    }


    private fun getDataSourceFactory(builder: DBIClientBuilder): PooledDataSourceFactory {

        return DataSourceFactory().apply {
            driverClass = "org.sqlite.JDBC"
            url = "jdbc:sqlite:/Users/simranjit.kaur/work/TodoAppDropwizard-master/todos.db"
        };
    }

}