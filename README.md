# Realizing dataflow tainting via non-intrusive Java Bytecode instrumentation

To see the bytecode for a class file, run ``javap -l -v -p -s TestString.class``

## Instrumentation

To run the instrumentation execute:
```bash
BASE_DIR=$(pwd)
gradle jar;
cd ${BASE_DIR}/build/libs;
java -jar asm_test-0.0.1-SNAPSHOT.jar -f ${BASE_DIR}/tests/TestString.class -o ${BASE_DIR}/tests/out/TestString.class
```
This will instrument the byte code of the input file (path after the -f flag) and write the result into the output file, given after the -o flag.
Input and output file must are not allowed to be the same (if so, it will fail).
The instrumented file can then be executed by running ``java -classpath ".:asm_test-0.0.1-SNAPSHOT.jar" TestString``.

### Select tainting method
To distinguish between the two tainting methods (boolean and range) there are commandline parameters depending on which kind of tainting is used (offline or agent).

For offline instrumentation add the following to the existing commandline arguments: ``--taintmethod range`` or ``--taintmethod boolean``

For agent instrumentation add the following key-value pair to the agent arguments: ``taintmethod=boolean`` or ``taintmethod=range``.
The agent definition in all in one looks then e.g. like this: ``-javaagent:asm_test-0.0.1-SNAPSHOT.jar=taintmethod=range``.

If no taint method is specified, boolean is used as default.  

### Logging

The instrumentation framework outputs a lot of information for debugging purposes. This can be disabled by setting the *log.level* property to *OFF*. E.g., by calling the instrumentation like this:

```bash
java -Dlog.level=OFF -jar asm_test-0.0.1-SNAPSHOT.jar -f ${BASE_DIR}/tests/TestString.class -o ${BASE_DIR}/tests/out/TestString.class
```

### Instrumenting a jar file

If one instruments a jar file, it is slightly more complicated. Due to [Stack Frame Verification](http://chrononsystems.com/blog/java-7-design-flaw-leads-to-huge-backward-step-for-the-jvm) the instrumenter needs to know common parent classes of types. It thus might have to load classes from the jar during instrumentation. Therefore one has to add the .jar to instrument to the classpath.

For some reason, the -classpath and the -jar switches of the java executable are mutually exclusive however. This doesn't seem to be properly documented either sadly.

So when instrumenting a jar, execute something like the following:
```sh
java -classpath "asm_test-0.0.1-SNAPSHOT.jar:jar-file-to-instrument-X.Y.Z.RELEASE.jar" de.tubs.cs.ias.asm_test.Main -f jar-file-to-instrument-X.Y.Z.RELEASE.jar -o jar-file-to-instrument-X.Y.Z.RELEASE.instrumented.jar
```
