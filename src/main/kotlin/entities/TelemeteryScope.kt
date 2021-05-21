package entities

import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import org.slf4j.MDC
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * This is used to manage lifecycle of [TelemetryFlowContext] so that [Telemetry] can function correctly in the
 * presence of both synchronous and async (coroutines) workflow.
 *
 * Every [TelemetryFlowContext] has a beginning, that is captured by initiating the appropriate [TelemetryScope], e.g.
 * for http requests, the whole servlet handler should be executed under [HttpRequestScope], while for flows that are
 * not http requests, but have a Job nature, e.g. cron jobs, should be executing under [JobScope]
 *
 * Once a flow has been initiated, [TelemetryScope.usingInheritedScope] can be used to resume coroutined workflows from
 * inside a non-coroutine layer of code
 *
 * After initiation, care must be taken NOT to switch threads outside of [withContext], as it will break the scope
 * inheritance logic
 */
abstract class TelemetryScope internal constructor(priorCoroutineContext: CoroutineContext): CoroutineScope {
    @Suppress("LeakingThis")
    override val coroutineContext: CoroutineContext = createCoroutineContext(priorCoroutineContext)

    private fun createCoroutineContext(priorCoroutineContext: CoroutineContext): CoroutineContext {
        val telemetryFlowContext = priorCoroutineContext[TelemetryFlowContext.Key]
            ?: throw IllegalArgumentException("TelemetryFlowContext MUST be provided")
        return Dispatchers.Default +
                SupervisorJob() +
                CoroutineName(telemetryFlowContext.id) +
                Slf4jContextElement(telemetryFlowContext.traceContext.traceId) +
                priorCoroutineContext +
                ctxTLS.asContextElement(ScopedElement(this))
    }

    /**
     * Executes [block] using this [TelemetryScope] in the current thread. This is helpful when we're running inside a
     * container that already has a large ThreadPool defined for processing, e.g. Jetty. If instead of executing on
     * the existing thread, we call a [CoroutineScope.launch], we may be burdening the ThreadPool on which coroutines
     * are running
     */
    fun<T> inCurrentThread(block: TelemetryScope.() -> T): T {
        // we have to set TLS manually here, because if we call TelemetryScope().inCurrentThread(), tls would not have
        // been set, because no suspension calls have been made
        val old = _thread_enter()
        try {
            return block(this)
        } finally {
            _thread_exit(old)
        }
    }

    /**
     * DO NOT USE unless you know what you're doing
     */
    @Suppress("FunctionName")
    fun _thread_enter(): Any? {
        val oldScopedElement = ctxTLS.get()
        val oldSlf4jContext = MDC.getCopyOfContextMap()
        coroutineContext.getTelemetryFlowContext()?.traceContext?.traceId?.let {
            updateSlf4jContext(createSlf4jContext(it))
        }
        ctxTLS.set(ScopedElement(this))
        return oldScopedElement to oldSlf4jContext // return a pair of oldScopedElement and oldSlf4jContext
    }

    /**
     * DO NOT USE unless you know what you're doing
     */
    @Suppress("FunctionName", "unchecked_cast")
    fun _thread_exit(returnValueFromEnter: Any?) {
        if (returnValueFromEnter != null) {
            val pair = returnValueFromEnter as Pair<ScopedElement, Map<String, String>>
            ctxTLS.set(pair.first)
            updateSlf4jContext(pair.second)
        } else {
            ctxTLS.remove()
            MDC.clear()
        }
    }

    companion object {
        /**
         * Run [block] under the inherited [TelemetryScope]
         *
         * @throws InvalidTelemetryScopeException if we are not inside a valid [TelemetryScope]
         */
        fun<T> usingInheritedScope(block: TelemetryScope.() -> T): T {
            val ctx = ctxTLS.get() ?: throw InvalidTelemetryScopeException()
            return slf4jBlock(ctx.scope) { block(ctx.scope) }
        }

        /**
         * Run [block] under the inherited [TelemetryScope], or under a [FakedScope]. STRICTLY to be used for migration
         * purposes only. Please see more comments in [FakedScope]
         */
        fun<T> usingInheritedScopeOrFaked(block: TelemetryScope.() -> T): T {
            val ctx = ctxTLS.get() ?: ScopedElement(FakedScope())
            return slf4jBlock(ctx.scope) { block(ctx.scope) }
        }

        /**
         * Schedule this job to be cancelled and waited on during shutdown
         */
        fun scheduleCancelAndWaitOnShutdown(job: Job) {
            Runtime.getRuntime().addShutdownHook(object: Thread() {
                override fun run() {
                    // currently just cancel until we figure out how to execute coroutines on a shutdown thread
                    job.cancel()
                    /*runBlocking {
                        job.cancelAndJoin()
                    }*/
                }
            })
        }

        /**
         * Helper function to start a new coroutine under the current [TelemetryScope] and return its result as
         * a [CompletableFuture]
         */
        fun<T> future(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T)
                : CompletableFuture<T> = usingInheritedScopeOrFaked {
            this.future(coroutineContext + context) {
                block(this)
            }
        }

        /**
         * Helper function to start a new coroutine under the current [TelemetryScope] and return its result as
         * a [Deferred]
         */
        @Suppress("unused")
        fun <T> async(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T): Deferred<T> =
            usingInheritedScopeOrFaked {
                this.async(coroutineContext + context) {
                    block(this)
                }
            }

        /**
         * Helper function to start a new coroutine under the current [TelemetryScope] and return the created [Job]
         */
        @Suppress("unused")
        fun launch(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit): Job =
            usingInheritedScopeOrFaked {
                this.launch(coroutineContext + context) {
                    block(this)
                }
            }

        /**
         * [kotlinx.coroutines.runBlocking] equivalent of TelemetryScope. This pulls the current (or faked) context,
         * and executes the suspending [block] in a blocking fashion
         */
        @Suppress("unused")
        @Throws(InterruptedException::class)
        fun <T> runBlocking(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T): T {
            return usingInheritedScopeOrFaked {
                kotlinx.coroutines.runBlocking(coroutineContext + context, block)
            }
        }

        /**
         * Checks if this is called from inside an existing TelemetryScope and throws InvalidTelemetryScopeException;
         * noop otherwise
         */
        @Suppress("unused")
        @Throws(InvalidTelemetryScopeException::class)
        fun validateNoInheritableScope() {
            ctxTLS.get()?.let {
                throw InvalidTelemetryScopeException()
            }
        }

        /**
         * Wrapper function to set mdc context and reset to old state once block finish execution
         */
        private fun <T> slf4jBlock(scope: CoroutineScope, block: CoroutineScope.() -> T): T {
            return scope.coroutineContext[Slf4jContextElement.Key]?.let { slf4jContext ->
                val oldContextMap = MDC.getCopyOfContextMap()
                MDC.setContextMap(slf4jContext.contextMap)
                try {
                    block(scope)
                } finally {
                    updateSlf4jContext(oldContextMap)
                }
            } ?: block(scope)
        }
    }
}

class InvalidTelemetryScopeException : Exception("no existing telemetry scope found")

private val ctxTLS = ThreadLocal<ScopedElement>()

private data class ScopedElement(val scope: TelemetryScope): AbstractCoroutineContextElement(ScopedElement) {
    override fun toString(): String = "ScopedElement(${this.scope.hashCode()})"

    companion object Key : CoroutineContext.Key<ScopedElement>
}

/**
 * TLS based context element to store slf4j context map that will get included in logs. Currently this includes only
 * traceId
 */
private class Slf4jContextElement(traceId: String): ThreadContextElement<Map<String, String>?>{
    companion object Key : CoroutineContext.Key<Slf4jContextElement>

    val contextMap = createSlf4jContext(traceId)
    override val key: CoroutineContext.Key<Slf4jContextElement> = Key

    override fun restoreThreadContext(context: CoroutineContext, oldState: Map<String, String>?) = updateSlf4jContext(oldState)

    override fun updateThreadContext(context: CoroutineContext): Map<String, String>? {
        val oldState = MDC.getCopyOfContextMap()
        updateSlf4jContext(contextMap)
        return oldState
    }
}

fun updateSlf4jContext(contextMap: Map<String, String>?) {
    /** as we can't set empty or null context throws null pointer exception at LogbackMDCAdapter **/
    if (contextMap == null) {
        MDC.clear()
    } else {
        MDC.setContextMap(contextMap)
    }
}

private fun createSlf4jContext(traceId: String): Map<String, String> = mapOf("TRACE-ID" to traceId)
