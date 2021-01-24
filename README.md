[![Maven Central](https://img.shields.io/maven-central/v/com.nordstrom.tools/java-utils.svg)](https://mvnrepository.com/artifact/com.nordstrom.tools/java-utils)

# NORDSTROM JAVA UTILS

**Nordstrom Java Utils** is a small collection of general-purpose utility classes with wide applicability.

## What You'll Find Here

* [ExceptionUnwrapper](#exceptionunwrapper) provides methods for extracting the contents of "wrapped" exceptions.
* [UncheckedThrow](#uncheckedthrow) provides a method that uses type erasure to enable you to throw checked exception as unchecked.
* [DatabaseUtils](#databaseutils) provides facilities that enable you to define collections of database queries and stored procedures in an easy-to-execute format.
  * [Query Collections](#query-collections) are defined as Java enumerations that implement the `QueryAPI` interface
  * [Stored Procedure Collections](#stored-procedure-collections) are defined as Java enumerations that implement the `SProcAPI` interface
  * [Recommended Implementation Strategies](#recommended-implementation-strategies) to maximize usability and configurability
    * [Query Collection Example](#query-collection-example)
  * [Registering JDBC Providers](#registering-jdbc-providers) with the **ServiceLoader** facility of **DatabaseUtils**
* [OSInfo](#osinfo)  provides utility methods and abstractions for host operating system features.
* [VolumeInfo](#volumeinfo) provides methods that parse the output of the 'mount' utility into a mapped collection of volume property records.
* [PathUtils](#pathutils) provides a method to acquire the next file path in sequence for the specified base name and extension in the indicated target folder.
* [Params Interface](#params-interface) defines concise methods for the creation of named parameters and parameter maps.
* [JarUtils](#jarutils) provides methods related to Java JAR files:
  * [Assembling a Classpath String](#assembling-a-classpath-string)
  * [Finding a JAR File Path](#finding-a-jar-file-path)
  * [Extracting the `Premain-Class` Attribute](#extracting-the-premain-class-attribute)

## ExceptionUnwrapper

The **ExceptionUnwrapper** class provides methods for extracting the contents of "wrapped" exceptions.

## UncheckedThrow

The **UncheckedThrow** class uses type erasure to enable client code to throw checked exceptions as unchecked. This allows methods to throw checked exceptions without requiring clients to handle or declare them. It should be used judiciously, as this exempts client code from handling or declaring exceptions created by their own actions. The target use case for this facility is to throw exceptions that were serialized in responses from a remote system. Although the compiler won't require clients of methods using this technique to handle or declare the suppressed exception, the JavaDoc for such methods should include a `@throws` declaration for implementers who might want to handle or declare it voluntarily.

```java
    ...
    
    String value;
    try {
        value = URLDecoder.decode(keyVal[1], "UTF-8");
    } catch (UnsupportedEncodingException e) {
        throw UncheckedThrow.throwUnchecked(e);
    }
    
    ...
```

## DatabaseUtils

**DatabaseUtils** provides facilities that enable you to define collections of database queries and stored procedures in an easy-to-execute format.

### Query Collections

Query collections are defined as Java enumerations that implement the `QueryAPI` interface:
* `getQueryStr` - Get the query string for this constant. This is the actual query that's sent to the database.
* `getArgNames` - Get the names of the arguments for this query. This provides diagnostic information if the incorrect number of arguments is specified by the client.
* `getConnection` - Get the connection string associated with this query. This eliminates the need for the client to provide this information.
* `getEnum` - Get the enumeration to which this query belongs. This enables `executeQuery(Class, QueryAPI, Object[])` to retrieve the name of the query's enumerated constant for diagnostic messages.
* ... see the _JavaDoc_ for the `QueryAPI` interface for additional information.

### Stored Procedure Collections

Store procedure collections are defined as Java enumerations that implement the `SProcAPI` interface: 
* `getSignature` - Get the signature for this stored procedure object. This defines the name of the stored procedure and the modes of its arguments. If the stored procedure accepts `varargs`, this will also be indicated (see _JavaDoc_ for details).
* `getArgTypes` - Get the argument types for this stored procedure object.
* `getConnection` - Get the connection string associated with this stored procedure. This eliminates the need for the client to provide this information.
* `getEnum` - Get the enumeration to which this stored procedure belongs. This enables `executeStoredProcedure(Class, SProcAPI, Object[])` to retrieve the name of the stored procedured's enumerated constant for diagnostic messages.
* ... see the _JavaDoc_ for the `SProcAPI` interface for additional information.

### Recommended Implementation Strategy

To maximize usability and configurability, we recommend the following implementation strategy:
* Define your collection as an enumeration:
  * Query collections implement the `QueryAPI` interface.
  * Stored procedure collections implement the `SProcAPI` interface.
* Define each constant:
  * (query) Specify a property name and a name for each argument (if any).
  * (sproc) Declare the signature and the type for each argument (if any).
* To assist users of your queries, preface their names with a type indicator (<b>GET</b> or <b>UPDATE</b>).
* Back query collections with configurations that implement the **`Settings API`**:
  * groupId: com.nordstrom.test-automation.tools
  * artifactId: settings
  * className: com.nordstrom.automation.settings.SettingsCore

* To support execution on multiple endpoints, implement `QueryAPI.getConnection()` or `SProcAPI.getConnection()` with sub-configurations or other dynamic data sources (e.g. - web service).

#### Query Collection Example

```java
public class OpctConfig extends SettingsCore<OpctConfig.OpctValues> {

    private static final String SETTINGS_FILE = "OpctConfig.properties";

    private OpctConfig() throws ConfigurationException, IOException {
        super(OpctValues.class);
    }

    public enum OpctValues implements SettingsCore.SettingsAPI, QueryAPI {
        /** args: [  ] */
        GET_RULE_HEAD_DETAILS("opct.query.getRuleHeadDetails"),
        /** args: [ name, zone_id, priority, rule_type ] */
        GET_RULE_COUNT("opct.query.getRuleCount", "name", "zone_id", "priority", "rule_type"),
        /** args: [ role_id, user_id ] */
        UPDATE_USER_ROLE("opct.query.updateRsmUserRole", "role_id", "user_id"),
        /** MST connection string */
        MST_CONNECT("opct.connect.mst"),
        /** RMS connection string */
        RMS_CONNECT("opct.connect.rms");

        private String key;
        private String[] args;
        private String query;

        private static OpctConfig config;
        private static String mstConnect;
        private static String rmsConnect;

        private static EnumSet<OpctValues> rmsQueries = EnumSet.of(UPDATE_USER_ROLE);

        static {
            try {
                config = new OpctConfig();
            } catch (ConfigurationException | IOException e) {
                throw new RuntimeException("Unable to instantiate OPCT configuration object", e);
            }
        }

        OpctValues(String key, String... args) {
            this.key = key;
            this.args = args;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public String getQueryStr() {
            if (query == null) {
                query = config.getString(key);
            }
            return query;
        }

        @Override
        public String[] getArgNames() {
            return args;
        }

        @Override
        public String getConnection() {
            if (rmsQueries.contains(this)) {
                return getRmsConnect();
            } else {
                return getMstConnect();
            }
        }

        @Override
        public Enum<OpctValues> getEnum() {
            return this;
        }

        /**
         * Get MST connection string.
         * 
         * @return MST connection string
         */
        public static String getMstConnect() {
            if (mstConnect == null) {
                mstConnect = config.getString(OpctValues.MST_CONNECT.key());
            }
            return mstConnect;
        }

        /**
         * Get RMS connection string.
         * 
         * @return RMS connection string
         */
        public static String getRmsConnect() {
            if (rmsConnect == null) {
                rmsConnect = config.getString(OpctValues.RMS_CONNECT.key());
            }
            return rmsConnect;
        }
    }

    @Override
    public String getSettingsPath() {
        return SETTINGS_FILE;
    }

    /**
     * Get OPCT configuration object.
     *
     * @return OPCT configuration object
     */
    public static OpctConfig getConfig() {
        return OpctValues.config;
    }

    public enum SProcValues implements SProcAPI {
        /** args: [  ] */
        SHOW_SUPPLIERS("SHOW_SUPPLIERS()"),
        /** args: [ coffee_name, supplier_name ] */
        GET_SUPPLIER_OF_COFFEE("GET_SUPPLIER_OF_COFFEE(>, <)", Types.VARCHAR, Types.VARCHAR),
        /** args: [ coffee_name, max_percent, new_price ] */
        RAISE_PRICE("RAISE_PRICE(>, >, =)", Types.VARCHAR, Types.REAL, Types.NUMERIC),
        /** args: [ str, val... ] */
        IN_VARARGS("IN_VARARGS(<, >:)", Types.VARCHAR, Types.INTEGER),
        /** args: [ val, str... ] */
        OUT_VARARGS("OUT_VARARGS(>, <:)", Types.INTEGER, Types.VARCHAR);

        private int[] argTypes;
        private String signature;

        SProcValues(String signature, int... argTypes) {
            this.signature = signature;
            this.argTypes = argTypes;
        }

        @Override
        public String getSignature() {
            return signature;
        }

        @Override
        public int[] getArgTypes () {
            return argTypes;
        }

        @Override
        public String getConnection() {
            return OpctValues.getRmsConnect();
        }

        @Override
        public Enum<SProcValues> getEnum() {
            return this;
        }
    }
}
```

### Registering JDBC Drivers

To provide maximum flexibility, JDBC interacts with database instances through a defined interface (**java.sql.Driver**). Implementations of this interface translate its methods into their vendor-specific protocol, in classes called **drivers**. For example, [OracleDriver](https://download.oracle.com/otn_hosted_doc/jdeveloper/905/jdbc-javadoc/oracle/jdbc/OracleDriver.html) enables JDBC to interact with Oracle database products.

In JDBC connection URLs, the vendor and driver are specified as suffixes to the `jdbc` protocol. For the Oracle "thin" driver, this is `jdbc:oracle:thin`. This protocol/vendor/driver combination is handled by **OracleDriver**, and JDBC needs this class to be registered to handle this vendor-specific protocol.

To simplify the process of registering vendor-specific JDBC drivers, **DatabaseUtils** loads these for you through the Java **ServiceLoader** facility. Declare the driver(s) you need in a **ServiceLoader** provider configuration file at **_META-INF/services/java.sql.Driver_**:

```
oracle.jdbc.OracleDriver
```

This sample provider configuration file will cause **DatabaseUtils** to load the JDBC driver class for Oracle database products. The JAR that declares this class needs to be on the class path for this to work. For Maven projects, you just need to add the correct dependency:

```xml
[pom.xml]
<project ...>
  [...]
  
  <dependencies>
    [...]
    <dependency>
      <groupId>com.oracle.jdbc</groupId>
      <artifactId>ojdbc6</artifactId>
      <version>11.2.0.4.0</version>
    </dependency>
  </dependencies>
  
  [...]
</project>
```

## OSInfo

The **OSInfo** class provides utility methods and abstractions for host operating system features.

## VolumeInfo

The **VolumeInfo** class provides methods that parse the output of the 'mount' utility into a mapped collection of volume property records.

## PathUtils

The **PathUtils** class provides a method to acquire the next file path in sequence for the specified base name and extension in the indicated target folder. If the target folder already contains at least one file that matches the specified base name and extension, the algorithm used to select the next path will always return a path whose index is one more than the highest index that currently exists. (If a single file with no index is found, its implied index is 0.)

##### Example usage of `getNextPath`

```java
    ...
    
    /*
     * This example gets the next path in sequence for base name `artifact`
     * and extension `txt` in the TestNG output directory.
     * 
     * For purposes of this example, the output directory already contains
     * the following files: `artifact.txt`, `artifact-3.txt`
     */

    Path collectionPath = Paths.get(testContext.getOutputDirectory());
    // => C:\git\my-project\test-output\Default suite

    Path artifactPath;
    try {
        artifactPath = PathUtils.getNextPath(collectionPath, "artifact", "txt");
        // => C:\git\my-project\test-output\Default suite\artifact-4.txt
    } catch (IOException e) {
        provider.getLogger().info("Unable to get output path; no artifact was captured", e);
        return;
    }
    
    ...
```

## Params Interface

The **Params** interface defines concise methods for the creation of named parameters and parameter maps. This facility can make your code much easier to read and maintain. The following example, which is extracted from the **Params** unit test class, demonstrates a few basic features.

#### Params Example
```
package com.nordstrom.example;

import static com.nordstrom.common.params.Params.param;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Optional;
import org.testng.annotations.Test;
import com.nordstrom.common.params.Params;

public class ParamTest implements Params {
    
    @Test
    public void testDefault() {
        assertFalse(Params.super.getParameters().isPresent());
    }
    
    @Test
    public void testParam() {
        Param param = param("boolean", true);
        assertEquals(param.getKey(), "boolean");
        verifyBoolean(param.getVal());
    }
    
    @Test
    public void testParams() {
        Optional<Map<String, Object>> optParameters = getParameters();
        assertTrue(optParameters.isPresent());
        Map<String, Object> parameters = optParameters.get();
        assertFalse(parameters.isEmpty());
        
        assertTrue(parameters.containsKey("boolean"));
        verifyBoolean(parameters.get("boolean"));
    }
    
    private void verifyBoolean(Object value) {
        assertTrue(value instanceof Boolean);
        assertTrue((Boolean) value);
    }
    
    @Override
    public Optional<Map<String, Object>> getParameters() {
        return Params.mapOf(param("boolean", true), param("int", 1), param("String", "one"),
                        param("Map", Params.mapOf(param("key", "value"))));
    }
}

```

This code uses a static import to eliminate redundant references to the **Params** interface. It also shows the unrestricted data types of parameter values. The use of **Optional** objects enables you to provide an indication that no value was returned without the risks associated with `null`.

## JarUtils

The **JarUtils** class provides methods related to Java JAR files. 

* `getClasspath` assemble a classpath string from the specified array of dependencies.
* `findJarPathFor` find the path to the JAR file from which the named class was loaded.
* `getJarPremainClass` gets the 'Premain-Class' attribute from the indicated JAR file.

The methods of this class provide critical services for the `Local Grid` feature of [**Selenium Foundation**](https://github.com/sbabcoc/Selenium-Foundation), handling the task of locating the JAR files that declare the classes required by the Java-based servers it launches.

### Assembling a Classpath String

The **`getClasspath`** method assembles a classpath string from the specified array of dependency contexts. This is useful for launching a Java sub-process, as it greatly simplifies the task of collecting the paths of JAR files that declare the classes required by your process. If any of the specified dependency contexts names the `premain` class of a Java agent, the string returned by this method will contain two records delimited by a `newline` character:

* `0` - assembled classpath string
* `1` - tab-delimited list of Java agent paths

### Finding a JAR File Path

The **`findJarPathFor`** method will find the absolute path to the JAR file from which the named class was loaded, provided the class has been loaded from a JAR file on the local file system.

### Extracting the `Premain-Class` Attribute

The **`getJarPremainClass`** method will extract the `Premain-Class` attribute from the manifest of the indicated JAR file. The value of this attribute specifies the name of a `Java agent` class declared by the JAR.

> Written with [StackEdit](https://stackedit.io/).