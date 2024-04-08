# How to Write a Fontus Configuration

To run your application with Fontus you have to write a matching configuration file. The configuration is fairly powerful but can be a bit daunting, so this document tries to get you started.

For the more advanced tricks you probably have to look at the code of Fontus and examples of successful configuration files.

The main structure is as follows:
```xml
<configuration>
    <excludedPackages>
    </excludedPackages>
    <vendors>
    </vendors>
    <purposes>
    </purposes>
    <sourceConfig>
        <sources>
        </sources>
    </sourceConfig>
    <sinkConfig>
        <sinks>
        </sinks>
    </sinkConfig>
    <converters>
    </converters>
    <returnGenerics>
    </returnGenerics>
    <takeGenerics>
    </takeGenerics>
    <passThroughTaints>
    </passThroughTaints>
</configuration>
```

We will go through all sections now in more detail.

## Excluded Packages

Some libraries simply do not work with Fontus or are not useful for tainting. If they are not security sensitive (That is something you have to analyze for your own application) and for example the performance impact of rewriting them is severe, you can try to exclude them from our instrumentation.

Please not that this exclusion is not particularly smart, so simply excluding random libraries will most likely break the application. This should be seen as a last resort!

What Fontus effectively does is when the ClassLoader passes it a Class to instrument, it checks whether the package name is included in `excludedPackages`, e.g., like this:

```xml
<excludedPackages>
    <excludedPackage>org/apache/tomcat/jni/</excludedPackage>
</excludedPackages>
```

Here `org/apache/tomcat/jni` is the package name we want to exclude and upon being handed a class to instrument which is locacted below said package, Fontus returns it unchanged. 
This might give a clue about the potential issues. If a class from an excluded package calls functions from instrumented classes (i.e., those not excluded nor in the standard library) the call will fail, as the instrumented class won't have to method the excluded class tries to call anymore.

For example consider a class called `Foo` in an excluded package `a/b`, with a method `void foo()` looking as follows:

```java
void foo() {
    b.c.Bar.test("just testing");
}
```
As `foo()` is in an excluded class, the method body is not instrumented. Consequently the call will look like this: `invokestatic b/c/Bar.test(Ljava/lang/String;)V`
However, as Bar was instrumented, the signature will have changed to take an taint aware String (i.e., `IASString`) instead. Attempting to call it with a regular String will fail.

**Therefore care has to be taken to only exclude leaf packages which do not call into not excluded packages!**

This is difficult to verify, but as said before, this is a last resort way to make an application work with Fontus. Making the exclusion mechanism smarter would be a great way to get familiar with Fontus and greatly enhance its usability. As certain packages do not interface with user facing strings but do string based computations and consequently face a heavy performance penalty once instrumented.


## Vendors (Related to GDPR Tainting)

This only should be set when using Fontus to prevent GDPR violating data flows. This section allows to list the vendors who receive data and process data in this application as follows:

```xml
<vendors>
    <vendor>
        <name>acme</name>
    </vendor>
    <vendor>
        <name>sap</name>
    </vendor>
</vendors>
```

## Purposes (Related to GDPR Tainting)

This, too, should only be set when using Fontus to prevent GDPR violating data flows. It allows to list the purposes for which the application processes data. Configuring them looks as follows:

```xml
<purposes>
    <purpose>
        <name>logging</name>
        <description>The data is used for logging information</description>
        <legal></legal>
    </purpose>
    <purpose>
        <name>marketing</name>
        <description>The data is used for marketing</description>
        <legal></legal>
    </purpose>
</purposes>
```

Each purpose is associated with 3 values, it's `name` which serves as a key internally. And to some day enable to display violations in a nicer fashion one can add a user facing and a legal description. Those can be used inside the taint handler to customize error messages for example.

## Sources

Taint tracking aims to track data flows between sources (i.e., where data enters the application) and sinks which process data in a sensitive fashion. The configuration of a source contains of 5 parts, of which two are mandatory (`name` and `function`) whereas the remainder are used for cases where you have to hack around the application semantics.
```xml
<source>
    <name>getParameterValues</name>
    <function>
    </function>
    <tainthandler>
    </tainthandler>
    <pass_locals>
    </pass_locals>
    <allowed_callers>
    </allowed_callers>
</source>
```

### Name

The identifier of the sink, per our unwritten convention this is usually the function name iff it is unique.

### Function

The descriptor of the function which shall be declared as a source. Not that for a source usually the return type should be a string. If it is a compound type of which one wants to taint specific properties and the default handler does not suffice, a manual taint handler has to be used. How to determine a function block is described at the end under `How to write a function block inside the config`.

### Taint Handler (optional)

Taint Handler are ways to customize how taints are set or checked. The default taint handler ([IASTaintHandler](https://github.com/SAP/project-fontus/blob/main/fontus/src/main/java/com/sap/fontus/taintaware/unified/IASTaintHandler.java)) tries to to either directly set the taint, if the return value implements [IASTaintAware](https://github.com/SAP/project-fontus/blob/main/fontus/src/main/java/com/sap/fontus/taintaware/IASTaintAware.java). Otherwise, it attempts to [traverse the object](https://github.com/SAP/project-fontus/blob/main/fontus/src/main/java/com/sap/fontus/taintaware/unified/IASTaintHandler.java#L100) to set properties or elements of an array.

While we tried to make this as generic as possible, it might be insufficient for some use cases. In those it is advisable to set your own taint handler. The same is true if one wants to use Fontus for GDPR enforcement, as the regular taint handler lacks the information required to set meaningful GDPR taints, e.g., how to retrieve an identifier for the current data subject.

A taint handler is set as follows:

```xml
<tainthandler>
    <opcode>184</opcode>
    <owner>com/sap/fontus/gdpr/openolat/OpenOlatTaintHandler</owner>
    <name>taint</name>
    <descriptor>(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;ILjava/lang/String;)Ljava/lang/Object;</descriptor>
    <interface>false</interface>
</tainthandler>
```

What the specific fields mean is explained at the end under `How to write a function block inside the config`.
It is important to not alter the function signature of your set taint hook if you provide your own taint handler. This is not verified by Fontus and consequently might cause issues if the signature does not match the one shown before, which is expected internally. This is probably something that Fontus should validate internally, so feel free to submit a PR (and adjust this document afterwards).

To understand the next section, it is important to explain what the parameters for the `taint` call actually do.

Looking at [the source](https://github.com/SAP/project-fontus/blob/main/fontus/src/main/java/com/sap/fontus/taintaware/unified/IASTaintHandler.java#L229) is usually the best way to get more insights, but the following should get you started. The signature with JavaDoc is:
```java
/**
  * Hook function called at all taint sources added to bytecode
  *
  * String object = parentObject.callToSink(parameters);
  *
  * @param object The object to be tainted (can be a string, or something which needs traversing, like a list)
  * @param parentObject this object on which the sink is being called
  * @param parameters the parameters passed to the sink function
  * @param sourceId The source as an integer
  * @return A tainted version of the input object
  */
public static Object taint(Object object, Object parentObject, Object[] parameters, int sourceId, String callerFunction)
```

1. The first parameter (called `object`) is the object which shall be tainted.
2. The `parentObject` value is the object on which the sink was called. This can be helpful to selectively taint objects based on a specific class inside some inheritance hierarchy.
3. The `parameters` array by default contains all arguments passed to the sink function. This can be helpful to for example set taints dependent on specific values passed into the sink function, e.g., if the function only performs a sensitive computation if a certain set of parameters is passed to it.
4. An identifier of the source. Internally Fontus assigns an integer id to each source/sink so one can identify the source this way. If one needs to access further information about the source function (e.g., the name), one can retrieve this via calling: `IASTaintSourceRegistry.getInstance().get(sourceId).getName()`.

The `taint` hook is then meant to return the input object (i.e., `object`) with its taints set accordingly.

#### Advanced Usages

As one can intercept and change the return values of arbitrary functions in this fashion, this mechanism can be used to hook into an application to adjust its runtime behavior. So if one really knows how Fontus works and how to debug instrumented applications, this is a really powerful tool to patch software on the fly.

### pass_locals (optional, advanced)

Sometimes it is fairly difficult to determine where to set the taint values. An example where we encountered difficulties in the past is tainting values inside HTML forms for POST requests. As there is no standardized mechanism the `taint` hook sometimes has to be placed at seemingly arbitrary points in the application. One thing that can happen is that the regularly provided information are insufficient for determining the correct taint status, we for example encountered this when trying to taint form values for OpenOlat. However, we observed that when inspecting the bytecode, the required information were available as locals (i.e., local variables on the Java source code level) inside the function we identified as sink candidate. Setting `pass_locals` allows one to add additional locals inside the function to the `parameters` array.

**This is fragile, as changes to the application might change the local numbers and thus break the taint hook!**

This should be used with care, as it is really easy to introduce hard to debug bugs this way. These bugs are only related to setting taints, so it is easier than most Fontus bugs, but if you get random values in your `parameters` array, check your locals.

### Allowed Callers (optional, advanced)

This is a defense mechanism to avoid needlessly placing `taint` hooks. As Fontus knows in which function the source was detected, this allows to set an allow list of functions for which the sink shall be applied.
Especially if the `pass_locals` directive is used, one should take care to analyze whether one can guard it this way!

### Full example

To show how such an complex source declaration might look like, the following provides a real example from OpenOlat to set taints for submitted forms. We identified that inside `org/olat/core/gui/components/form/flexible/impl/Form.doInitRequestMultipartDataParameter()` the class `IOUtils` from the apache commons library was tasked with parsing and retrieving values from the multipart encoded input data. Consequently, `IOUtils.toString()` as used as the sink. However, only tainting the return value of the call would not have allowed us to set the data protection metadata correctly, as this method is not concerned with the Servlet from which the data originates but which holds the reference to access session related properties. However, we found that a reference to the OpelOlat Request wrapper was stored as local 1 inside the calling function. Therefore we had to pass additional values used inside `doInitRequestMultipartDataParameter` to the taint handler to correctly set the taint metadata. As the `IOUtils.toString()` method is generic and might be used at different places throughout OpenOlat for unrelated purposes (and with different locals available) we safeguarded the source setting by restricting the allowed callers to only `doInitRequestMultipartDataParameter`. This led us to the following:

```xml
<source>
    <!-- invokestatic  #852                // Method org/apache/commons/io/IOUtils.toString:(Ljava/io/InputStream;Ljava/nio/charset/Charset;)Lcom/sap/fontus/taintaware/unified/IASString; -->
    <name>ioutils-tostring</name>
    <function>
        <opcode>184</opcode>
        <owner>org/apache/commons/io/IOUtils</owner>
        <name>toString</name>
        <descriptor>(Ljava/io/InputStream;Ljava/nio/charset/Charset;)Ljava/lang/String;</descriptor>
        <interface>false</interface>
    </function>
    <tainthandler>
        <opcode>184</opcode>
        <owner>com/sap/fontus/gdpr/openolat/OpenOlatTaintHandler</owner>
        <name>formTaint</name>
        <descriptor>(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;ILjava/lang/String;)Ljava/lang/Object;</descriptor>
        <interface>false</interface>
    </tainthandler>
    <pass_locals>
        <pass_locals>1</pass_locals>
        <pass_locals>5</pass_locals>
    </pass_locals>
    <allowed_callers>
        <function>
            <opcode>183</opcode>
            <owner>org/olat/core/gui/components/form/flexible/impl/Form</owner>
            <name>doInitRequestMultipartDataParameter</name>
            <descriptor>(Lorg/olat/core/gui/UserRequest;)V</descriptor>
            <interface>false</interface>
        </function>
    </allowed_callers>
</source>
```

### Sink

From a high level perspective a sink looks as follows:

```xml
<sink>
    <name></name>
    <categories>
    </categories>
    <function>
    </function>
    <parameters>
    </parameters>
    <dataProtection>
    </dataProtection>
</sink>
```

We will now dive into the specific sections and explain their semantics and when you need them.

#### Name

Unique identifier for the sink, to be able to provide meaningful error messages in case a taint flow was detected.

#### Function

The function that shall be treated as a sink. How to determine the value of such a function block is described at the end under `How to write a function block inside the config`.

#### Parameters

A sink function might process a multitude of values, of which only few are security sensitive. Take for example a `void setString(int index, String value)` method of a (fictional) `SQLStatement` which assembles an SQL query based on some skeleton and replaces specific placeholders via `setString`. If it does string manipulation internally and  one wants to protect against SQL injection, this would be a prime example for a sink. Now `setString` takes two arguments, the `index` and the `value`. Calling the `checkTaint` hook for both parameters would be wasteful, as `index` is of type `int` which Fontus can't taint anyway. Consequently it would make sense to restrict the taint check to the second parameter. This is possible by setting the parameters field. Parameter indices start at 0. 

It is also possible to apply the taint check to the return value of a function call. This can be achieved by adding the -1 parameter to the list of parameters to assess. This can be helpful in the data protection case if you e.g., have code like this:

```java
for(int id : getAllUserIds(id)) {
    User u = getUser(id);
    // Taint loss occurs here, e.g., due to String -> char array conversion
    sensitiveFunction(untainted);
}
```

The optimal sink could be `sensitiveFunction` but if for some reason we'd lose the taint in between, we have to come up with a way to still check the taint. Here care has to be taken to only taint return values of functions which are not called at other, unrelated, places. There is a safeguard for this, but this is currently lacking documentation. Please open an issue if you run into such a problem.

### Data Protection (optional, related to GDPR enforcement)

This allows to set the data protection properties of a sink, e.g., to specific for which `purposes` and by which `vendors` data processing takes place inside this function.

A fully configured data protection section looks as follows:

```xml
<dataProtection>
    <aborts>
        <abort>censor</abort>
        <abort>stderr_logging</abort>
    </aborts>
    <purposes>
        <purpose>emails</purpose>
    </purposes>
    <vendors>
        <vendor>sap</vendor>
    </vendors>
</dataProtection>
```

Here, the vendor `sap` would process personal data for the purpose of sending `emails`. If a purpose violation is detected, e.g., because the incoming piece of personal information does not have `emails` among its allowed purposes, the `abort` handlers are called (in order). Here, one can basically configure how the application should react to such a purpose violation. In the example the data would be censored (i.e., masked with `***`) and an error logged to stderr. Other possible ways to react is to throw an exception or to write your own logic. To add new logic to react to such a violation, you can look at the available abortion handlers [here](https://github.com/SAP/project-fontus/blob/main/fontus/src/main/java/com/sap/fontus/config/abort/Abort.java) and write your own, similar to the [censor](https://github.com/SAP/project-fontus/blob/main/fontus/src/main/java/com/sap/fontus/config/abort/CensoringAbort.java) one.


## Converters

At times when interacting with the standard library, one can run into type mismatches at runtime. This can occur if JDK functions take/return `Object` but expect them to be of a specific type, usually `String`. Due to Java's messed up generics, this is sadly not all that uncommon. Fontus tries to add type conversion calls if it detects a potentially problematic call, but this is sometimes insufficient and one has to provide hints on how to deal with a specific function call. In rare cases this can be helpful for non JDK functions too.

**If you notice that a converter for a JDK function is missing from the default config, please open a Pull Request with the entry**.  The JDK logic is applicable for every application and therefore we are happy to ship an example config which includes as many of them as possible. Please avoid submitting pull requests or adding converters which add random dependencies to Fontus. If it is some really common library, one can attempt to write a converter such as the [DGMMethodConverter](https://github.com/SAP/project-fontus/blob/main/fontus/src/main/java/com/sap/fontus/manual/groovy/converters/DGMMethodConverter.java#L10) for Groovy. One can see that all calls are done via reflection which avoids a direct dependency. The presence of such converters generally makes it very difficult to verify the configuration, as actually loading the class to check for its presence might cause `ClassNotFound` exceptions, as the converter has to interact with the specified library classes.

Generally, the preferred form of converter is a call to a function in e.g., [IASStringUtils](https://github.com/SAP/project-fontus/blob/main/fontus/src/main/java/com/sap/fontus/taintaware/unified/IASStringUtils.java). A converter looks as follows:

```java
<converter>
    <opcode>184</opcode>
    <owner>com/sap/fontus/manual/spring/converters/ObjectConverter</owner>
    <name>convertToStringIfPossible</name>
    <descriptor>(Ljava/lang/Object;)Ljava/lang/Object;</descriptor>
    <interface>false</interface>
</converter>
```

This basically describes the call to [ObjectConverter.convertToStringIfPossible](https://github.com/SAP/project-fontus/blob/main/fontus/src/main/java/com/sap/fontus/manual/spring/converters/ObjectConverter.java#L9).

What's important is that the `name` attribute is used as the key to look it up later, so avoid name clashes!

This section only makes the list of available converters known to Fontus, the following sections determine how they are applied.

## Returns Generic (Converter related)

If a call to a JDK method returns a e.g., List of Strings, this information is lost at the bytecode level, due to type erasure (great feature, .. not). An example for such a function is [RuntimeMXBean.getInputArguments()](https://docs.oracle.com/javase/8/docs/api/java/lang/management/RuntimeMXBean.html#getInputArguments--). In our experience the default conversion strategy of Fontus is insufficient in this case, consequently it makes sense to apply a converter manually. This is done as follows:
```xml
<returnGeneric>
    <function>
        <opcode>185</opcode>
        <owner>java/lang/management/RuntimeMXBean</owner>
        <name>getInputArguments</name>
        <descriptor>()Ljava/util/List;</descriptor>
        <interface>true</interface>
    </function>
    <converter>convertStringList</converter>
</returnGeneric>
```

Here, the `function` block describes the function which returns a value we have to convert and the `converter` attribute signals which converter shall be used to to perform the actual conversion. To apply a converter, when method JDK calls, Fontus looks in the list of `returnGeneric` values to check whether the function to call requires conversion and if that is the case, it tries to retrieve the converter with a matching `name`. If this succeeds it converts the return value of the function before continuing to regular operation.

## Takes Generic (Converter related

Similar to return values, JDK method call parameters might require conversion too, usually converting taint aware to regular Strings. An example for such a function is the constructor for `ProcessBuilder` which takes a list of Strings. To prevent the application from crashing, we have to apply a converter which turns it into a regular list first.

This would look like the following:

```xml
<takeGeneric>
    <function>
        <opcode>183</opcode>
        <owner>java/lang/ProcessBuilder</owner>
        <name>&lt;init&gt;</name>
        <descriptor>(Ljava/util/List;)V</descriptor>
        <interface>false</interface>
    </function>
    <conversions>
        <conversion>
            <converter>convertTStringList</converter>
            <index>0</index>
        </conversion>
    </conversions>
</takeGeneric>
```

The function block describes for which function the conversion(s) shall be applied. Note that we have to escape the `<init>` name for the XML configuration. 

As different parameters might require different conversion strategies, one has to list each parameter and converter pair. The process to apply them is basically the same as for returning generic values.

## Pass Through Taints (Advanced)

Sometimes one has to interact with function that will lose the taint if its inputs. If this is encapsulated nicely, this section of the config allows one to attempt a quick workaround. It also is fairly restricted at the moment, i.e., it only works for functions of type `String -> String`.

The idea is to store the taint before invoking the lossy function and then reassign the taint to the return value. Currently, if the length of the string changes this might crash.

To configure this, one would write something such as:
```xml
<passThroughTaints>
    <passThroughTaint>
        <opcode>185</opcode>
        <owner>org/olat/core/util/filter/Filter</owner>
        <name>filter</name>
        <descriptor>(Ljava/lang/String;)Ljava/lang/String;</descriptor>
        <interface>true</interface>
    </passThroughTaint>
</passThroughTaints>
```

This function does some filtering against e.g., XSS, by iterating over single chars and assembling a new, cleaned String. This is a lossy operation and consequently we can simply carry the taint over this function. 


**This is a ugly hack and should only be used if really necessary**

## How to write a function block inside the config

All over the config one has to specify functions that Fontus has to recognize. The current config format closely resembles the way a function call looks like in bytecode. So, if you have set up your application and want to check that it works (without any advanced configuration), it is advisable to set the `verbose` flag. The tells Fontus to save all class files to disk after instrumentation. The class files are stored in `./tmp/agent/package/ClassName.class` format. To inspect them, use e.g., `javap -l -v -p -s ClassName.class` which will print the bytecode of the class in question. Java bytecode is fairly easy to read, but even if unfamiliar with the bytecode the important part, the function calls which `javap` will fully resolve for you, are easy to locate.

For example, the following line turns up inside some OpenOlat classes:

```java
15: invokevirtual #260                // Method org/olat/user/UserImpl.getProperty:(Lcom/sap/fontus/taintaware/unified/IASString;)Lcom/sap/fontus/taintaware/unified/IASString;
```

The leading `15:` is unimportant, as is the `#260` identifier. What we need to know is the bytecode instruction used to invoke the function (here, `invokevirtual`), the fully qualified class name (here, `org/olat/user/UserImpl`), the function name (here, `getProperty`) and its descriptor, here `(Lcom/sap/fontus/taintaware/unified/IASString;)Lcom/sap/fontus/taintaware/unified/IASString;`. 

With this information, we can already fill in the `owner` and `name` attribute of a function block. We are now still missing `opcode`, `descriptor` and `interface`.
The opcode can be directly translated by looking up the numeric value [here](https://asm.ow2.io/javadoc/constant-values.html#org.objectweb.asm.Opcodes.INVOKEDYNAMIC), for `invokevirtual` this results in 182. 
The descriptor has to be translated back to its untainted form for setting sources, sinks, or to configure pass through taints. This can be done by replacing all taint aware classes by their fully qualified JDK classes. For our example we'd translate `(Lcom/sap/fontus/taintaware/unified/IASString;)Lcom/sap/fontus/taintaware/unified/IASString;` to `(Ljava/lang/String;)Ljava/lang/String;`.
Lastly, the `interface` key signals if the method's owner class is an interface. If you are unsure try to set it to `false`.

The full function block would look as follows:
```xml
<function>
    <opcode>182</opcode>
    <owner>org/olat/user/UserImpl</owner>
    <name>getProperty</name>
    <descriptor>(Ljava/lang/String;)Ljava/lang/String;</descriptor>
    <interface>false</interface>
</function>
```

Then, after configuring Fontus with out custom sources and sinks, run it again with the `verbose` flag and check whether Fontus added the call to the source/sink hooks around the method in question. If the hook call is missing, there is probably a mismatch between config and bytecode. This can take some trial and error is unfamiliar with Java internals.

Suggestions/Ways to make this nicer would be greatly appreciated.

## Start a new configuration

Writing a new configuration from scratch is quite a bit of work, so try to have a look at existing ones. For example the HTTP sources are usually the same, so you don't have to figure those out on your own and can copy them. The `resources` directory has some examples on how to configure Fontus against injection vulnerabilities. How to configure data protection can be seen by looking at the provided examples [here](./gdpr_configs)





