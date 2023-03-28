# Adding Taint Persistence

0. Please ensure your application works properly as is! Adding taint persistence will make debugging just more difficult/annoying if something is currently broken anyway.

1. Ensure the Application runs with MySQL

Adjust the configuration accordingly! In theory other DBMS are also supported, but those might require (hopefully small) changes to the driver. If something is not generic please feel free to open an issue or preferable a pull request.

2. Dump the Database

With something like `mysqldump -u $USER -p "$DBNAME" >> "$DBNAME.sql"` you can dump the database to a SQL file. Before continuing ensure the dump contains both the creation of the schema (all DDL required to setup the tables) as well as the data (a bunch of insert statements).

To do this with a running docker container, you can run:
```
docker exec -i $CONTAINER_NAME mysqldump -u $USER -p "$DBNAME" >> "$DBNAME.sql
```

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

