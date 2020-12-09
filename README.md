# mapfill
`mapfill` is a small utility program for adding meta-data to [openLCA mapping
files](https://github.com/GreenDelta/olca-modules/blob/master/doc/flow_mapping_csv_format.md).
It takes a CSV mapping file and a data source, adds the meta-data of the flows
to the input or output side of the mapping file, and writes the updated mappings
back to the file.

## Usage

```
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
```
