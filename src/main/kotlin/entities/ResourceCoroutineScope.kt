package entities

import javax.ws.rs.container.AsyncResponse
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.ws.rs.core.Response

fun<T> AsyncResponse.with(block: suspend () -> T)
    {
        GlobalScope.launch(start = CoroutineStart.UNDISPATCHED)
        {
            try{
                val response = block()
                this@with.resume(when(response)
                {
                    is Unit -> Response.noContent().build()
                    null -> Response.status(Response.Status.NOT_FOUND).build()
                    else -> response
                })
            } catch (t: Throwable)
            {
                this@with.resume(t)
            }
        }
    }
