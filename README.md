[![REUSE status](https://api.reuse.software/badge/github.com/SAP/project-fontus)](https://api.reuse.software/info/github.com/SAP/project-fontus)

# Project Fontus

A modern framework for dynamic taint analysis with string-like classes in the JVM.

## About this project

Dynamic tainting framework for Java applications leveraging on-the-fly bytecode rewriting.

## Requirements and Setup

For building the framework execute the gradle task ``shadowJar`` or ``publishToMavenLocal``. Afterwards you will find the Framework JAR in ``./fontus/build/libs``

## Building additional tools

In the [tools](./tools) folder some tools related to Fontus are provided. They can be build via `/gradlew tools:TOOLNAME:jar` and the resulting jar is stored in the corresponding build folder of the tool. For Example, to build the SQL definition taint jar, invoke: `./gradlew tools:sql-tainter:jar` and then run it via `java -jar ./tools/sql-tainter/build/libs/sql-tainter.jar <inputfile>`.

The provided tools are the following:

### [SQL Tainter](./tools/sql-tainter)

Takes a .sql file as input, taints all included statements and writes them to `tainted_<inputfilename>.sql`.

### [GDPR Database query (db-query)](./tools/gdpr-database-query)

Queries a provided database for GDPR tainting related questions. Can be used to realize the following tasks: Subject Access Request, Collect expired PII, contesting wrong data and to collect PII statistics.

### [Converter](./tools/converter)
Converts a Juturna configuration to a Fontus one. Untested by me, as I have no access to Juturna source code.

### [Generator](./tools/generator)

Generates a source and sink configuration for the passed classes. No idea how this works either!

## Agent Instrumentation
This instrumentation type works on-the-fly with starting the application.

### Execution
For instrumenting via java agents just add the following to your VM option parameters:
```bash
--add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/jdk.internal.vm.annotation=ALL-UNNAMED -javaagent:fontus-0.0.1-SNAPSHOT.jar
```

The `--add-opens` are necessary because Fontus is using Java internal classes

A complete java execution command could look like this:
```bash
java --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/jdk.internal.vm.annotation=ALL-UNNAMED -jar your-application.jar -javaagent:fontus-0.0.1-SNAPSHOT.jar
```

### Parameters
It is also possible to pass multiple parameters to the agent
- **verbose**: If this option is set, all instrumented classes are saved to ``./tmp/agent``
- **logging_enabled**: If this option is set, a log file of the instrumentation process will be created in the working dir named ``asm-{datetime}.log`` 
- **taintmethod**: Specifying the used taint method. For all options see [Available Tainting Methods](#Available Tainting Methods). The default is *boolean*
- **use_caching**: Possible values: *true* or *false*. Default is true. Enables/Disables caching of taint evaluation results for lazy tainting methods
- **layer_threshold**: Specifies a maximum depth of layers for lazybasic tainting. If this threshold is exceeded the taint is calculated and new layers will be stacked on top again. Default value is *30*. If caching is disabled, the threshold is also disabled.
- **collect_stats**: Possible values: *true* or *false*. Default is false. If this option is enabled, the stats about taints in strings will be collected. This only applies iff taintmethod *range* is used and can cause massive overhead.
- **config**: Specifies a path for a config file
- **blacklisted_main_classes**: Specifies a filepath to a file which contains blacklisted main classes
- **abort**: Specifies what happens if a tainted string reaches a sink. For all options see [Abort types](#Abort types). The default is *stderr_logging*
- **taintloss_handler**: Specifies what happens if a method is called which potentially causes taintloss (e.g. String.toCharArray()). For all options see [Taintloss handler types](#Taintloss handler types). By default no taintloss handler is used 

The arguments are appended to the agent path like this: ``-javaagent:jarpath[=options]``. Therefore options are defined as ``key=value`` pair and ``,`` is used as delimiter between key-value-pairs.

An example for parameters passed to the agent ``-javaagent:"fontus-0.0.1-SNAPSHOT.jar=taintmethod=range,use_caching=false,verbose"``.

## Available Tainting Methods
Currently there are 5 different tainting mechanisms available:
- **boolean**: Only tainting per string. Differentiation which character is tainted is *not* possible. Very fast, little memory overhead, but more false positives
- **array**: Naive tainting per character. Differentiation which character is tainted *is* possible. Linear overhead regarding length for CPU and memory (slow and expensive), nearly no false positives.
- **range**: Optimized tainting per character. Differentiation which character is tainted *is* possible. Linear overhead regarding count of taints per string for CPU and memory (most times a lot more efficient than *array*). As precise as *array*.
- **lazybasic**: Optimized range approach. Differentiation which character is tainted *is* possible. As long as no taint evaluation is done, faster than range. Memory overhead mostly correlates with the number of string manipulations. As precise as *array*.
- **lazycomplex**: Optimized lazybasic approach. Differentiation which character is tainted *is* possible. Less computation effort during runtime and during taint evaluation. Memory overhead mostly correlates with the number of string manipulations. As precise as *array*.
- **untainted**: An wrapper class is used to redirect all calls to the original classes. No taint calculation is performed! The taint is always "false"

## Abort types
Currently there are four possibilities what can happen, if a tainted string reaches a sink:

- **exit**: Exits the application through System.exit(int). Beforehand the string is printed to stderr
- **nothing**: Nothing happens if a tainted string reaches a sink
- **stderr_logging**: Logs the tainted string to stderr as well as an stacktrace
- **json_logging**: Logs the tainted string to a JSON file in ``./fontus-results.json``

## Taintloss handler types
- **stderr_logging**: Logs to stderr if a potentially taintlossy method is called
- **file_logging**: Logs to file``./taintloss.log`` formatted in the same way we stderr_logging
- **statistics_logging**: Logs to the statistics MXBean in the format "Caller.method -> Taintloss.method: Hits"

## Inspect Bytecode of a class

To see the Bytecode for a class file, run ``javap -l -v -p -s TestString.class``

## Troubleshoot

Have a look in the [docs folder](./docs)!

## Support, Feedback, Contributing

This project is open to feature requests/suggestions, bug reports etc. via [GitHub issues](https://github.com/SAP/project-fontus/issues). Contribution and feedback are encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](CONTRIBUTING.md).

## Security / Disclosure
If you find any bug that may be a security problem, please follow our instructions at [in our security policy](https://github.com/SAP/project-fontus/security/policy) on how to report it. Please do not create GitHub issues for security-related doubts or problems.

## Code of Conduct

We as members, contributors, and leaders pledge to make participation in our community a harassment-free experience for everyone. By participating in this project, you agree to abide by its [Code of Conduct](https://github.com/SAP/.github/blob/main/CODE_OF_CONDUCT.md) at all times.

## Licensing

Copyright 2024 SAP SE or an SAP affiliate company and project-fontus contributors. Please see our [LICENSE](LICENSE) for copyright and license information. Detailed information including third-party components and their licensing/copyright information is available [via the REUSE tool](https://api.reuse.software/info/github.com/SAP/project-fontus).

