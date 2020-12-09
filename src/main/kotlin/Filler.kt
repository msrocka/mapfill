import org.openlca.io.maps.FlowMap
import org.openlca.io.maps.FlowRef
import org.openlca.util.Strings

class Filler(
    private val flowMap: FlowMap,
    private val side: Side,
    private val collector: Collector) {

    fun doIt() {
        println("collect flows from data source ... ")
        val metaRefs = collector.collect()
        println("collected ${metaRefs.size} flows")

        println("update mappings ...")
        var count = 0
        flowMap.entries.forEach { entry ->
            val mapRef = if (side == Side.Source) {
                entry.sourceFlow
            } else {
                entry.targetFlow
            }
            if (mapRef?.flow == null)
                return@forEach
            val metaRef = metaRefs[mapRef.flow.refId]
                ?: return@forEach
            if (update(mapRef, metaRef)) {
                count++
            }
        }
        println("updated $count mappings")
    }

    private fun update(mapRef: FlowRef, metaRef: FlowRef): Boolean {
        var updated = false

        // category path
        if (on(mapRef.flowCategory, metaRef.flowCategory)) {
            mapRef.flowCategory = metaRef.flowCategory
            updated = true
        }

        // location
        if (on(mapRef.flowLocation, metaRef.flowLocation)) {
            mapRef.flowLocation = metaRef.flowLocation
            updated = true
        }

        // flow name
        if (metaRef.flow != null) {
            if (mapRef.flow == null) {
                mapRef.flow = metaRef.flow
                updated = true
            } else if (on(mapRef.flow.name, metaRef.flow.name)) {
                mapRef.flow.name = metaRef.flow.name
                updated = true
            }
        }

        // flow property
        if (metaRef.property != null) {
            if (mapRef.property == null) {
                mapRef.property = metaRef.property
                updated = true
            } else {
                if (on(mapRef.property.refId, metaRef.property.refId)) {
                    mapRef.property.refId = metaRef.property.refId
                    updated = true
                }
                if (on(mapRef.property.name, metaRef.property.name)) {
                    mapRef.property.name = metaRef.property.name
                    updated = true
                }
            }
        }

        // unit
        if (metaRef.unit != null) {
            if (mapRef.unit == null) {
                mapRef.unit = metaRef.unit
                updated = true
            } else {
                if (on(mapRef.unit.refId, metaRef.unit.refId)) {
                    mapRef.unit.refId = metaRef.unit.refId
                    updated = true
                }
                if (on(mapRef.unit.name, metaRef.unit.name)) {
                    mapRef.unit.name = metaRef.unit.name
                    updated = true
                }
            }
        }

        return updated
    }

    private fun on(mapVal: String?, metaVal: String?): Boolean {
        return Strings.nullOrEmpty(mapVal) && Strings.notEmpty(metaVal)
    }
}