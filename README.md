# Realizing dataflow tainting via non-intrusive Java Bytecode instrumentation

Inside the tests folder are some .java Files used to test the instrumentation approach.

For the instrumentation to work, compile the java source files with the JDK8 java compiler. Support for JDK9+ is under development.

To see the bytecode for a class file, run ``javap -l -v -p -s TestString.class``

To run the instrumentation execute:
```bash
BASE_DIR=$(pwd)
gradle jar;
cd ${BASE_DIR}/build/libs;
java -jar asm_test-0.0.1-SNAPSHOT.jar -f ${BASE_DIR}/tests/TestString.class -o ${BASE_DIR}/tests/out/TestString.class
```
This will instrument the byte code of the input file (path after the -f flag) and write the result into the output file, given after the -o flag.
For the tests to succeed it is important that the bytecode of the IASString.java and IASStringBuilder.java files are present in the output folder. The instrumentation tool can automatically check this if the "-c" flag is present.
The instrumented file can then be normally executed by running ``java TestString``.
