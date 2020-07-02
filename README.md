# Realizing dataflow tainting via non-intrusive Java Bytecode instrumentation
A modern framework for dynamic taint analysis with string-like classes in the JVM

## Building the Framework
For building the framework execute the gradle task ``jar``. Aftwerwords you will find the Framework JAR in ``./build/libs``

There will be two JAR files. First of all the one starting with ``asm_test``, which is for doing the offline instrumentation and the agent instrumentation.
And secondly the one which starts with ``util``, which is necessary to execute offline instrumented applications. 

## Offline instrumentation
### Instrumentation
#### Class files
To instrument a class file you have to execute the ``asm_test`` JAR and passing the parameters ``-f`` for the source file and ``-o`` for the output file.

**Attention**: Source and output cannot be the same, otherwise the instrumentation will fail

Here is an example how to instrument a class file:
```bash
BASE_DIR=$(pwd)
cd ${BASE_DIR}/build/libs;
java -jar asm_test-0.0.1-SNAPSHOT.jar -f ${BASE_DIR}/tests/TestString.class -o ${BASE_DIR}/tests/out/TestString.class
```

#### JAR Archives
If one instruments a jar file, it is slightly more complicated. Due to [Stack Frame Verification](http://chrononsystems.com/blog/java-7-design-flaw-leads-to-huge-backward-step-for-the-jvm) the instrumenter needs to know common parent classes of types. It thus might have to load classes from the jar during instrumentation. Therefore one has to add the .jar to instrument to the classpath.

For some reason, the -classpath and the -jar switches of the java executable are mutually exclusive however. This doesn't seem to be properly documented either sadly.

So when instrumenting a jar, execute something like the following:
```sh
java -classpath "asm_test-0.0.1-SNAPSHOT.jar:jar-file-to-instrument-X.Y.Z.RELEASE.jar" de.tubs.cs.ias.asm_test.Main -f jar-file-to-instrument-X.Y.Z.RELEASE.jar -o jar-file-to-instrument-X.Y.Z.RELEASE.instrumented.jar
```

#### Select taint method
To choose a specific taint method use the command line parameter ``--taintmethod method``, just replace ``method`` with a valid taint method (see [Available Tainting Methods](#Available Tainting Methods)).

### Execution
The instrumented file can then be executed by running ``java -classpath ".:util-0.0.1-SNAPSHOT.jar" TestString``.

### Logging

The instrumentation framework outputs a lot of information for debugging purposes. This can be disabled by setting the *log.level* property to *OFF*. E.g., by calling the instrumentation like this:

```bash
java -Dlog.level=OFF -jar asm_test-0.0.1-SNAPSHOT.jar -f ${BASE_DIR}/tests/TestString.class -o ${BASE_DIR}/tests/out/TestString.class
```

## Agent Instrumentation
This instrumentation type works on-the-fly with starting the application.

### Execution
For instrumenting via java agents just add the following to your VM option parameters:
```bash
-javaagent:asm_test-0.0.1-SNAPSHOT.jar
```

A complete java execution command could look like this:
```bash
java -jar your-application.jar -javaagent:asm_test-0.0.1-SNAPSHOT.jar
```

### Parameters
It is also possible to pass multiple parameters to the agent
- **taintmethod**: Specifying the used taint method. For all options see [Available Tainting Methods](#Available Tainting Methods). The default is *boolean*
- **use_caching**: Possible values: *true* or *false*. Default is true. Enables/Disables caching of taint evaluation results for lazy tainting methods
- **layer_threshold**: Specifies a maximum depth of layers for lazybasic tainting. If this threshold is exceeded the taint is calculated and new layers will be stacked on top again. Default value is *30*. If caching is disabled, the threshold is also disabled.
- **count_ranges**: Possible values: *true* or *false*. Default is false. If this option is enabled, the number of taint ranges per created string is saved and every 100 created strings statistics will be printed out in stdout. This only applies if taintmethod *range* is used.
- **config**: Specifies a path for a config file
- **blacklisted_main_classes**: Specifies a filepath to a file which contains blacklisted main classes

The arguments are appended to the agent path like this: ``-javaagent:jarpath[=options]``. Therefore options are defined as ``key=value`` pair and ``;`` is used as delimiter between key-value-pairs.

An example for parameters passed to the agent ``-javaagent:asm_test-0.0.1-SNAPSHOT.jar=taintmethod=range;use_caching=false``.

### Logging

The instrumentation framework outputs a lot of information for debugging purposes. This can be disabled by setting the *log.level* property to *OFF*. E.g., by calling the instrumentation like this:

```bash
java -Dlog.level=OFF -jar your-application.jar -javaagent:asm_test-0.0.1-SNAPSHOT.jar
```

## Available Tainting Methods
Currently there are 5 different tainting mechanisms available:
- **boolean**: Only tainting per string. Differentiation which character is tainted is *not* possible. Very fast, little memory overhead, but more false positives
- **array**: Naive tainting per character. Differentiation which character is tainted *is* possible. Linear overhead regarding length for cpu and memory (slow and expensive), nearly no false positives.
- **range**: Optimized tainting per character. Differentiation which character is tainted *is* possible. Linear overhead regarding count of taints per string for cpu and memory (most times a lot more efficient than *array*). As precise as *array*.
- **lazybasic**: Optimized range approach. Differentiation which character is tainted *is* possible. As long as no taint evaluation is done, faster than range. Memory overhead mostly correlates with the number of string manipulations. As precise as *array*.
- **lazycomplex**: Optimized lazybasic approach. Differentiation which character is tainted *is* possible. Less computation effort during runtime and during taint evaluation. Memory overhead mostly correlates with the number of string manipulations. As precise as *array*.

## Inspect bytecode of a class
To see the bytecode for a class file, run ``javap -l -v -p -s TestString.class``
