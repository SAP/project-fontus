# Integrating Fontus with other tools

## JaCoCo (Coverage measurements)

In case you want to measure the code coverage of the program instrumented with Fontus, you can use it as follows:

```bash
FONTUS_PATH="$HOME/Projects/TU_BS/java_bytecode_rewriting/Fontus/fontus/build/libs/fontus-0.0.1-SNAPSHOT.jar"
JACOCO_PATH="$HOME/Projects/TU_BS/java_bytecode_rewriting/jacoco/lib/jacocoagent.jar"

java -jar --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  -javaagent:"${FONTUS_PATH}=verbose" \
  -javaagent:"${JACOCO_PATH}" \
"$@"
```

Running Fontus after JaCoCo on the other hand does not work.

### JaCoCo with Docker

Now, when running a web application inside docker, it gets slightly more involved. The above does not work, as during container teardown the coverage report isn't written out properly.

Instead, you have to run it in server mode. This can be done as follows:

#### Preparation
1. Add the JaCoCo files to your Docker container.
This can be done like this:
```dockerfile
WORKDIR /jacoco
RUN wget https://repo1.maven.org/maven2/org/jacoco/jacoco/0.8.8/jacoco-0.8.8.zip
RUN unzip jacoco-0.8.8.zip

```
Ensure to 'reset' the working directory to not change where fontus logs class files.

2. Add JaCoCo agent to your JVM invocation.

For example, when using tomcat you can add the following to your `JAVA_OPTS`:
```bash
-javaagent:/jacoco/lib/jacocoagent.jar=output=tcpserver,address=*
```
This will start a tcpserver listening on port *6300* which takes JaCoCo `dump`
requests. This port has to be exposed in the docker-compose setup too, e.g.,
via:

```yaml
ports:
  - "127.0.0.1:6300:6300"
```

Ensure to start Fontus with the `verbose` option and make the resulting
class files accessible for the report later on.

#### Getting Coverage Statistics

1. Run the application

Do your worst!

2. Get the coverage stats

Assuming you expose the JaCoCo port on localhost with the default port as in
the examples above, simply run the following to dump coverage statistics into
a file called `jacoco.exec`:
```bash
java -jar jacococli.jar dump --destfile=jacoco.exec
```
3. Generate the report

To generate the report JaCoCo needs read access to the class files, so adjust their permissions accordingly.

Assume the class files are located in `temp/agent`, you can generate an HTML report in the folder `cov_report` as follows:
```bash
java -jar jacococli.jar report jacoco.exec --classfiles temp/agent/ --html cov_report
```

If you want to have more control over what is included in the coverage
statistics, you can either change the [agent's parameters](https://www.jacoco.org/jacoco/trunk/doc/agent.html) to selectively collect coverage statistics. (Untested afaik)
Alternatively, you can generate a CSV report by adding `--csv report.csv` to the report generation invocation. In the [utils](./../utils/README.md) folder there is a script to filter the CSV report. **This is recommended for usage in a paper, as you get numbers without manual extraction**.
