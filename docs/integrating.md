# Integrating Fontus with other tools

## Jacoco (Coverage measurements)

In case you want to measure the code coverage of the program instrumented with Fontus, you can use it as follows:

```bash
FONTUS_PATH="$HOME/Projects/TU_BS/java_bytecode_rewriting/Fontus/fontus/build/libs/fontus-0.0.1-SNAPSHOT.jar"
JACOCO_PATH="$HOME/Projects/TU_BS/java_bytecode_rewriting/jacoco/lib/jacocoagent.jar"

java -jar --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  -javaagent:"${FONTUS_PATH}=verbose" \
  -javaagent:"${JACOCO_PATH}" \
"$@"
```

Running Fontus after Jacoco on the other hand does not work.
