package entities

import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Context for all information relevant to a particular telemetry flow, e.g. an http request, or a scheduled job
 */
class TelemetryFlowContext internal constructor(
    val id: String,
    val type: String,
    /**
     * [TraceContext] corresponding to this flow
     */
    val traceContext: TraceContext,
    /**
     * Properties that will get included in [Telemetry] when persisting
     */
    val properties: ConcurrentHashMap<String, String> = ConcurrentHashMap(),
    /**
     * Measures to be included in [Telemetry] when persisting
     */
    val measures: ConcurrentHashMap<String, Double> = ConcurrentHashMap()
): AbstractCoroutineContextElement(TelemetryFlowContext) {
    init {
        properties["telemetry_type"] = type
        properties["parent_id_for_children"] = traceContext.parentIdForMyChildren
    }
    override fun toString(): String = "TelemetryFlowContext(trace=$traceContext)"

    companion object Key : CoroutineContext.Key<TelemetryFlowContext>
}

/**
 * Get the current [TelemetryFlowContext] if available
 */
fun CoroutineContext.getTelemetryFlowContext(): TelemetryFlowContext? = this[TelemetryFlowContext.Key]

