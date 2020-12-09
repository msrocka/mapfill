import org.openlca.io.Format
import org.openlca.io.maps.FlowMap
import org.openlca.util.Strings
import java.io.File
import java.lang.Exception

class Config {
    var mappingFile = ""
    var side = ""
    var source = ""
}

enum class Side {
    Source,
    Target
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printHelp()
        return
    }

    // parse arguments
    var flag: String? = null
    val config = Config()
    for (arg in args) {
        if (arg == "-h" || arg == "help") {
            printHelp()
            return
        }
        if (arg.startsWith("-")) {
            flag = arg
            continue
        }
        if (flag == null) {
            println(
                "ERROR: invalid arguments; " +
                        "use `help` for more information"
            )
            return
        }
        when (flag) {
            "-map" -> config.mappingFile = arg
            "-on" -> config.side = arg
            "-from" -> config.source = arg
        }
    }

    val side = sideOf(config)
    val mapping = mappingOf(config)
    val collector = collectorOf(config)
    if (side == null || mapping == null || collector == null) {
        println(
            "ERROR: invalid arguments; " +
                    "use `help` for more information"
        )
        return
    }

    Filler(mapping, side, collector).doIt()


}

private fun printHelp() {
    println(
        """
        The mapfill command takes the following arguments:

        mapfill -map [m] -on [o] -from [f]

        with:

        * m : the path to the CSV mapping file
        * o : indicates which flows of the mapping should be updated:
              'source' or 's': for the flows on the source side
              'target' or 't': for the flows on the target side
        * f : the path of the file that contains the meta-data, this can be
              * an EcoSpold 1 zip file
              * an EcoSpold 2 zip file
              * an ILCD package
              * an openLCA database directory
              * a SimaPro CSV file

        `mapfill -h` or `mapfill help` will print this help
    """.trimIndent()
    )
}

private fun sideOf(config: Config): Side? {
    val side = config.side.trim().toLowerCase()
    return when {
        side.startsWith("s") -> Side.Source
        side.startsWith("t") -> Side.Target
        else -> null
    }
}

private fun mappingOf(config: Config): FlowMap? {
    val path = config.mappingFile
    if (Strings.nullOrEmpty(path))
        return null
    val file = File(path)
    if (!file.exists()) {
        println("ERROR: $file does not exist")
        return null
    }
    return try {
        FlowMap.fromCsv(file)
    } catch (e: Exception) {
        println("ERROR: failed to read mapping file: ${e.message}")
        null
    }
}

private fun collectorOf(config: Config): Collector? {
    val path = config.source
    if (Strings.nullOrEmpty(path))
        return null
    val file = File(path)
    if (!file.exists()) {
        println("ERROR: $file does not exist")
        return null
    }
    // if (Derby.isDerbyFolder(file))
    // DBCollector
    val format = Format.detect(file).orElse(null)
    if (format == null) {
        println("ERROR: could not detect format of $file")
        return null
    }
    return when (format) {
        Format.ILCD_ZIP -> ILCDCollector(file)
        else -> {
            println("ERROR: format $format is not" +
                    " implemented as data source")
            null
        }
    }
}