import java.io.File

fun main(args: Array<String>) {
    val ilcd = "C:/Users/Win10/Projects/databases/_dumps/EF-v3.0.zip"
    ILCDCollector(File(ilcd)).collect()
}