package entities
/**
 * [TelemetryScope] for faked scenarios. This is to be STRICTLY used only for migration purposes, where some code paths
 * need an existing [TelemetryScope] for [Telemetry] to function, but may be getting called in legacy non-scoped paths
 * as well.
 */
class FakedScope: TelemetryScope(TraceContext.generateNew().let {
    TelemetryFlowContext(
        id = "faked-${it.parentId}",
        traceContext = it,
        type = "FAKED"
    )
})
