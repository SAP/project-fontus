# Utils

Shell scripts to make working with Fontus easier (maybe!)

Those are not always tested on different systems, so your mileage may vary..

## [Code Coverage](./coverage.py)

Based on jacoco .csv reports it can calculate code coverage limited to specific packages.

Call it like the following:
```bash
python coverage.py --input="$HOME/Projects/TU_BS/gdpr/Broadleaf-Heatclinic-taintable/report.csv"  --keep org.broadleafcommerce --block org.broadleafcommerce.core.web.checkout.service
```

This used the report given as parameter via the *input* flag and filters it to all fully qualified names starting with *org.broadleafcommerce* but not with *org.broadleafcommerce.core.web.checkout.service*. 
Both `--keep` and `--block` take several parameters like this: `--keep a b c`

## [List JDK classes](./list.jdk.classes)

Extracts a list of classes belonging to a Java JDK

## [Count Handler Lines of Code](./count_handler_loc.sh)

Extracts the number of lines of actual Java code for each GDPR Taint Handler. Requires [cloc](https://github.com/AlDanial/cloc) in your PATH.
