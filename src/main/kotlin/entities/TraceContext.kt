package entities

import kotlin.random.Random

data class TraceContext(
    /**
     * Trace id is a unique string that is shared across a distributed operation, e.g. if udaan-web receives a
     * request, that trace id would be shared between all distributed calls (including nested ones) that are made
     * in the call chain of that request
     */
    val traceId: String,
    /**
     * Parent id represents the id as received from the caller by the current system. This will be changed at every
     * step in a distributed call chain, e.g. if udaan-web receives a request with
     * (traceId=c7151ed4a34cb4960c4adb354cf17a75, parentId=3d8252081e0733b1), it will call udaan-search using
     * (traceId=c7151ed4a34cb4960c4adb354cf17a75, parentId=53c763472a1dcc8e)
     *
     * This should remain the same for siblings of the same parent context
     */
    val parentId: String
) {
    val parentIdForMyChildren: String = getHexRandom(8)
    /**
     * Generate a new [TraceContext] for a child call
     */
    fun newChildContext(): TraceContext = TraceContext(
        traceId = traceId,
        parentId = parentIdForMyChildren
    )

    override fun toString(): String = "TraceContext(traceId=$traceId,parentId=$parentId)"

    companion object {
        /**
         * Generate a completely new [TraceContext]
         */
        fun generateNew(): TraceContext = TraceContext(
            traceId = getHexRandom(16),
            parentId = getHexRandom(8)
        )
    }
}

/**
 * Helper function to generate random hex strings
 */
private fun getHexRandom(octets: Int): String = when(octets) {
    4 -> "%02x".format(Random.nextInt()).ensurePaddedHex(octets)
    8 -> "%02x".format(Random.nextLong()).ensurePaddedHex(octets)
    16 -> getHexRandom(8) + getHexRandom(8)
    else -> StringBuilder()
        .apply {
            Random.nextBytes(octets).forEach {
                append("%02x".format(it).ensurePaddedHex(1))
            }
        }
        .toString()
}

/**
 * Ensure padding of hex if the initial few bits were 0
 */
private fun String.ensurePaddedHex(octets: Int) =
    if (length < octets*2) padStart(octets*2, '0') else this

