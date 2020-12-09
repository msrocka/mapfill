import org.openlca.core.model.descriptors.FlowDescriptor
import org.openlca.core.model.descriptors.FlowPropertyDescriptor
import org.openlca.core.model.descriptors.UnitDescriptor
import org.openlca.ilcd.commons.FlowType
import org.openlca.ilcd.commons.LangString
import org.openlca.ilcd.flowproperties.FlowProperty
import org.openlca.ilcd.flows.Flow
import org.openlca.ilcd.io.ZipStore
import org.openlca.ilcd.units.Unit
import org.openlca.ilcd.units.UnitGroup
import org.openlca.io.maps.FlowRef
import org.openlca.util.Strings
import java.io.File

/**
 * Collects elementary flow data from an ILCD package.
 */
class ILCDCollector(private val zip: File) : Collector {

    override fun collect(): Map<String, FlowRef> {

        val store = ZipStore(zip)

        // collect the reference units
        val refUnits = mutableMapOf<String, Unit>()
        store.each(UnitGroup::class.java) { group ->
            val refIdx = group.unitGroupInfo
                ?.quantitativeReference
                ?.referenceUnit
                ?: return@each
            val refUnit = group
                .unitList
                ?.units
                ?.find { unit -> unit.id == refIdx }
                ?: return@each
            refUnits[group.uuid] = refUnit
        }

        // collect the flow property names and bind
        // the reference units to the flow property IDs
        val propertyNames = mutableMapOf<String, String>()
        store.each(FlowProperty::class.java) { prop ->
            propertyNames[prop.uuid] = LangString.getVal(prop.name, "en")
            val groupID = prop.flowPropertyInfo
                ?.quantitativeReference
                ?.unitGroup
                ?.uuid
                ?: return@each
            val unit = refUnits[groupID] ?: return@each
            refUnits[prop.uuid] = unit
        }

        // collect the information from the elementary flows
        val flowRefs = mutableMapOf<String, FlowRef>()
        store.each(Flow::class.java) { flow ->
            val type = flow.modelling
                ?.lciMethod
                ?.flowType
                ?: return@each
            if (type != FlowType.ELEMENTARY_FLOW)
                return@each

            val ref = FlowRef()
            ref.flow = FlowDescriptor()
            ref.flow.refId = flow.uuid
            ref.flow.name = LangString.getVal(flow.name, "en")
            ref.flowCategory = categoryOf(flow)
            ref.flowLocation = flow.flowInfo
                ?.geography
                ?.location
                ?.find { s -> Strings.nullOrEmpty(s.lang) || s.lang == "en" }
                ?.value

            flowRefs[ref.flow.refId] = ref

            // add flow property information
            val refPropIdx = flow.flowInfo
                ?.quantitativeReference
                ?.referenceFlowProperty
                ?: return@each
            val refPropID = flow.flowPropertyList
                ?.flowProperties
                ?.find { p -> p.dataSetInternalID == refPropIdx }
                ?.flowProperty
                ?.uuid
                ?: return@each
            ref.property = FlowPropertyDescriptor()
            ref.property.refId = refPropID
            ref.property.name = propertyNames[refPropID]

            // add unit information
            val unit = refUnits[refPropID] ?: return@each
            ref.unit = UnitDescriptor()
            ref.unit.name = unit.name
        }

        store.close()
        return flowRefs
    }

    private fun categoryOf(flow: Flow): String? {
        val compartments = flow.flowInfo
            ?.dataSetInfo
            ?.classificationInformation
            ?.compartmentLists
            ?.get(0)
            ?.compartments
        if (compartments != null && compartments.isNotEmpty()) {
            compartments.sortBy { c -> c.level }
            return compartments.fold(null) { path: String?, c ->
                if (path == null)
                    c.value
                else
                    path + "/" + c.value
            }
        }

        val categories = flow.flowInfo
            ?.dataSetInfo
            ?.classificationInformation
            ?.classifications
            ?.get(0)
            ?.categories
        if (categories != null && categories.isNotEmpty()) {
            categories.sortBy { c -> c.level }
            return categories.fold(null) { path: String?, c ->
                if (path == null)
                    c.value
                else
                    path + "/" + c.value
            }
        }

        return null
    }

}