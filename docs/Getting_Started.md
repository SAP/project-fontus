# Getting started with Fontus!

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

To add taint persistence, proceed to the taint persistence section.


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

## Adding Taint Persistence

0. Please ensure your application works properly as is! Adding taint persistence will make debugging just more difficult/annoying if something is currently broken anyway.

1. Ensure the Application runs with MySQL

Adjust the configuration accordingly! In theory other DBMS are also supported, but those might require (hopefully small) changes to the driver. If something is not generic please feel free to open an issue or preferable a pull request.

2. Dump the Database

With something like `mysqldump -u $USER -p "$DBNAME" >> "$DBNAME.sql"` you can dump the database to a SQL file. Before continuing ensure the dump contains both the creation of the schema (all DDL required to setup the tables) as well as the data (a bunch of insert statements).

3. Taint the DB dump

The `com.sap.fontus.sql.tainter.SQLRewriter` class has a main method to taint all SQL statements in a text file. This assumes the input file (called `dump.sql`) is in the current working directory. If run that way it will create a `tainted_dump.sql` file containing all tainted statements.

If this worked without errors (haha), go to TODO.

4. There were a bunch of errors...

Sadly this is somewhat expected too. The used parser does not understand every bit of weird SQL Syntax and does not support some normal statements either.. So try fixing up the `dump.sql` file before running the tainter again.

Some things I noticed:

- `LOCK <FOO>` and `UNLOCK <FOO>` cause the tainter to crash. As you surely are not importing the dump while using the DB, just remove those statements.
- Some DB specific index syntax. Remove and add back manually.

Those are all crashes in the frontend (the SQL Parser). Sadly we can't do much here without rewriting it to work with a different/better parser. If you have suggestions on a better library or how to fix JSQLParser (The grammar is insane) this, please get in touch!

5. Reimport the tainted SQL

Restore the dump via something like ``mysql -u $USER -p "$DBNAME" < "tainted_$DBNAME.sql"``

If this fails, there are two options:

- You missed something in step 4 and thus there are missing statements, e.g., you try to insert something into a table where the create statement is missing. Go directly back to to Step 4. Do not pass GO, do not collect 200 bucks.
- The Tainter mangled something badly, please open [an issue](https://git.ias.cs.tu-bs.de/GDPR_Tainting/Fontus/issues?labels=123)

6. Adjust the application to use taint persistence

- Driver name

Add or adjust the used JDBC driver name:
```ini
spring.datasource.driver-class-name=com.sap.fontus.sql.driver.tubsDriver
```

This class is shipped with Fontus and thus automatically available in the class path

- Connection String

Assume you have a connection string looking as follows:
```ini
spring.datasource.url=jdbc:mysql://${MYSQL_HOST:mysql}:3306/petclinic?serverTimezone=UTC
```

Here the driver understanding `mysql` is used. Change this as follows:

```ini
spring.datasource.url=jdbc:tubs:mysql://${MYSQL_HOST:mysql}:3306/petclinic?serverTimezone=UTC
```

Just adding the `tubs:` part in the connection string results in the application looking for a driver knowing `tubs` (ours!) first. Our driver will then look for the driver for the corresponding DBMS (here `mysql`) and load it.

- Connection Pooling

Depending on how the database connection is set up, your application will probably use connection pooling. Connection pools might wrap Connection objects to return basically facades to Connections from a cache.

This wrapping breaks the type introspection Fontus relies upon to detect taint persistence.

This can be fixed by switching to a connection pool compatible with Fontus.

For example HikariCP is known to not work, while tomcat-jdbc works.

Example for fixing a spring boot application using spring data-jpa:

```xml
<!-- tomcat-jdbc works with Fontus while HikariCP causes taint losses, I have an idea on how to fix this tho.. -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
  <exclusions>
    <exclusion>
      <groupId>com.zaxxer</groupId>
      <artifactId>HikariCP</artifactId>
    </exclusion>
  </exclusions>
</dependency>
<dependency>
  <groupId>org.apache.tomcat</groupId>
  <artifactId>tomcat-jdbc</artifactId>
  <version>9.0.10</version>
</dependency>
```

Additionally please add the following to your `application.properties`:
```ini
spring.datasource.tomcat.use-statement-facade=false
```
This allows the type introspection for the varying Statement objects.

7. It works!

Great, enjoy your full stack taint enabled application! :)

8. It works, but crashes for some database entries

The taint driver has a weird issue with empty taint fields. I actually have no idea what's missing/going on here. This can happen if you taint the database and run the application without the taint driver. Look in the database for all fields with empty taint values and change them to `0`. Even better would be a fix to either the taint driver or the SQLRewriter.

9. It works but there are exceptions inside the taint driver

Please check that the columns don't have empty values (see Step 8.). If that is not the case, please open [an issue](https://git.ias.cs.tu-bs.de/GDPR_Tainting/Fontus/issues?labels=123) with a minimal working example for us to reproduce. Just based on a stack trace it is impossible to fix.
