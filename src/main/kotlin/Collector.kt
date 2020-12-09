import org.openlca.io.maps.FlowRef

/**
 * Collects flow information from a data source.
 */
interface Collector {

    fun collect(): Map<String, FlowRef>

}