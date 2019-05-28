# Bytecode Instrumentation System Tests

This test framework is meant to execute a wide range of Java applications once without instrumentation and once instrumented.

It is supposed to check that the applications behave the same before and after instrumentation by comparing their outputs/return values. Thus, this framework is meant as a sanity check to ensure that changes to our instrumentation don't break basic functionality.

## How to add an application to the test harness

First one has to decide whether one wants to add a single class file test application (preferable to test simple functionality, e.g., like a regression test) or a larger test case consisting of multiple class files.

### Single .java file based application

Simply add an entry in the *single_file_tests* array in *config.json*.

The following fields are required:

- name: The test's name, formatted for the user.
- source: The name of the .java file in the src folder.

The following fields are optional:

- arguments: A String array consisting of command line arguments the harness shall pass to the application.

### A .jar based application

This case is a bit more complex, as we have to compile the applications first.

#### Building the .jar
Currently it is supposed to only test .jar files we compile ourselves. Thus each application has a subfolder under jars. That folder has to contain a *build.sh* Shell script which builds the application. It is recommended for the build script to not leave behind any temporary files.

If the shell script requires an external build tool (that isn't required by the system anyway, like Gradle) this should be documented somewhere! As those tools need to be installed when running tests inside a container.

Once the build.sh script exists, add the folder to the main build script (build.sh in this folder)

#### Configuration entry

Now add an entry in the *jar_tests* array in *config.json*.

The following fields are required:

- name: The test's name, formatted for the user.
- jar_file The name of the .jar file, as built by the corresponding build.sh script.

The following fields are optional:

- entry_point: The class containing the main Method can be given. If it is not explicitly stated, *Main* is assumed.
- arguments: A String array consisting of command line arguments the harness shall pass to the application.
- input_file: A file with user inputs that should be used as standard input. This allows to simulate more complex (command line based) user interactions.
