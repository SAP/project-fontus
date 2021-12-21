# Getting started with Fontus!

## General Remarks

If you have an issue with the documentation or there is a mistake, feel free to fix it or open [an issue](https://git.ias.cs.tu-bs.de/GDPR_Tainting/Fontus/issues) (tagged with documentation please)!

## Preliminary Steps

First you have to build Fontus as follows:
```sh
./gradlew clean shadowJar publishToMavenLocal
```

We publish to Maven local as it simplifies the following steps.

## Running your first Application

0. Ensure the application runs regularly.

If there are issues in the first place, Fontus will be of little help to fix them ;) It will only get more annoying..

1. Prepare your configuration

Interesting are the possibilities to blocklist packages (treat them as if they belong to the Java Standard Library, thus they remain uninstrumented) and the converter logic.

Converters are useful if there are type mismatches (Lists of IASStrings that the JVM expects to be regular Strings) or if some postprocessing can fix a messed up implementation (e.g., replace some literal values in a data structure)

For the remainder of the section I assume the configuration is called `configuration.xml` and placed in the root folder of your application.

2. Rebuild the application and note the command

E.g., ``./mvnw clean package -DskipTests`` as well as the resulting executable jar/war filename (called `$APPNAME` in the following).

3. Run it!

I use the following shell script:

```sh
#!/bin/bash

APPNAME="$(pwd)/target/app.jar"
FONTUS_PATH="$HOME/.m2/repository/com/sap/fontus/fontus/0.0.1-SNAPSHOT/fontus-0.0.1-SNAPSHOT.jar"
CONFIG_PATH="$(pwd)/configuration.xml"

./mvnw clean package  -DskipTests
java -jar --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  -javaagent:"${FONTUS_PATH}=config=${CONFIG_PATH},abort=stderr_logging" "${APPNAME}"
```

If you are really lucky, it just works..

To add taint persistence, proceed to the [taint persistence](Taint_persistence.md) documentation.


4. It crashed :(

This is (sadly) the somewhat expected outcome. Now the fun begins!

5. Enable logging

Fontus has some flags to aid debugging:

- enable_logging: Writes a very detailed log file.

You can find the log in the folder you started the script. In the log file there is everything you might want to know about what methods were instrumented, skipped, ... This can/will get really large. This helps with questions on whether a sink check was actually inserted and so one.

- verbose: Log the instrumented class files.

Here you can inspect the bytecode Fontus did generate. This helps with debugging e.g., type errors caused by the instrumentation or suspicious taint losses.

In the folder you started the run script, there is a `tmp/agent` folder containing all `*.class` files. The JDK ships with the `javap` tool to inspect class files, so I suggest to start here.

I have aliased `javapp` to `javap -l -v -p -s` as that's (in my opinion, YMMV!) the most helpful combination of flags to inspect what Fontus actually did. If you have other tips on using `javap` or know other/nicer/better tools please add them here!

6. Stil stuck, let's try a debugger:

To attach a debugger add the following to the run script:

```sh
-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 \
```

The JVM will now suspend execution and wait for a remote debugger to be attached on port 5005 before running the application.

Please be aware that your favorite IDE will probably show you the application's source code, which is not what the JVM executes! Fontus adds/changes methods so you have to be aware on how the instrumentation actually changes the code underneath.

Example: IntelliJ allows for conditional break points and to evaluate expressions in the debugger. Assume you want to conditionally trigger a breakpoint if a string variable's (called `v`) value is equal to say `"java.lang.String"`. The straightforward approach (`v.equals("java.lang.String")` will always be false. Fontus has changed `v`to be of the type `IASString` and thus it can't be equal to a regular String literal. Instead you have to look for a method taking a `CharSequence` for the comparison to work. So the condition `v.conentEquals("java.lang.String")` should work.

7. I'm still stuck!

Open a [Git Issue](https://git.ias.cs.tu-bs.de/GDPR_Tainting/Fontus/issues). The more detailed the input, the more likely we can help you. Important is a workable way to reproduce the bug on one of our machines!

## Debugging Tips

```java
setter.equals(item.getClass().getMethod("getName").invoke(item))
```
