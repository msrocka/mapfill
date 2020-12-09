import java.io.File

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


    val ilcd = "C:/Users/Win10/Projects/databases/_dumps/EF-v3.0.zip"
    ILCDCollector(File(ilcd)).collect()
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
