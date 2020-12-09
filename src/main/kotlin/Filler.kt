import org.openlca.io.maps.FlowMap

class Filler(
    private val flowMap: FlowMap,
    private val side: Side,
    private val collector: Collector) {

    fun doIt() {
        println("Collect flows from data source ... ")
        val flows = collector.collect()
        println("Collected ${flows.size} flows")


    }
}