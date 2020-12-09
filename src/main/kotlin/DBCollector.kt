import org.openlca.core.database.derby.DerbyDatabase
import org.openlca.core.model.Flow
import org.openlca.core.model.FlowType
import org.openlca.core.model.descriptors.Descriptor
import org.openlca.core.model.descriptors.FlowDescriptor
import org.openlca.io.maps.FlowRef
import org.openlca.util.Categories
import java.io.File

class DBCollector(private val dir: File) : Collector {

    override fun collect(): Map<String, FlowRef> {
        val refs = mutableMapOf<String, FlowRef>()
        val db = DerbyDatabase(dir)
        val descriptors = db.allDescriptorsOf(Flow::class.java)
        for (d in descriptors) {
            if (d !is FlowDescriptor)
                continue
            if (d.flowType != FlowType.ELEMENTARY_FLOW)
                continue

            val flow = db.get(Flow::class.java, d.id)
                ?: continue

            val ref = FlowRef()
            ref.flow = d
            ref.flowLocation = flow.location?.code

            if (flow.category != null) {
                val path = Categories.path(flow.category)
                if (path.isNotEmpty()) {
                    ref.flowCategory = path.joinToString("/")
                }
            }

            val prop = flow.referenceFlowProperty
            if (prop != null) {
                ref.property = Descriptor.of(prop)
            }

            val unit = flow.referenceUnit
            if (unit != null) {
                ref.unit = Descriptor.of(unit)
            }

            refs[d.refId] = ref
        }
        db.close()
        return refs
    }
}