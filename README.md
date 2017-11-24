[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.nordstrom.tools/java-utils/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nordstrom.tools/java-utils)

# NORDSTROM JAVA UTILS

**Nordstrom Java Utils** is a small collection of general-purpose utility classes with wide applicability.

## UncheckedThrow

The **UncheckedThrow** class uses type erasure to enable client code to throw checked exceptions as unchecked. This allows methods to throw checked exceptions without requiring clients to handle or declare them. It should be used judiciously, as this exempts client code from handling or declaring exceptions created by their own actions. The target use case for this facility is to throw exceptions that were serialized in responses from a remote system. Although the compiler won't require clients of methods using this technique to handle or declare the suppressed exception, the JavaDoc for such methods should include a `@throws` declaration for implementers who might want to handle or declare it voluntarily.


```java
    ...
    
    String value;
    try {
        value = URLDecoder.decode(keyVal[1], "UTF-8");
    } catch (UnsupportedEncodingException e) {
        UncheckedThrow.throwUnchecked(e);
    }
    
    ...
```

## DatabaseUtils

The **DatabaseUtils** class provides facilities that enable you to define collections of Oracle database queries and execute them easily. Query collections are defined as Java enumerations that implement the `QueryAPI` interface:
* `getQueryStr` - Get the query string for this constant. This is the actual query that's sent to the database.
* `getArgNames` - Get the names of the arguments for this query. This provides diagnostic information if the incorrect number of arguments is specified by the client.
* `getArgCount` - Get the number of arguments required by this query. This enables **DatabaseUtils** to verify that the correct number of arguments has been specified by the client.
* `getConnection` - Get the connection string associated with this query. This eliminates the need for the client to provide this information.
* `getEnum` - Get the enumeration to which this query belongs. This enables **DatabaseUtils** to retrieve the name of the query's enumerated constant for diagnostic messages.

To maximize usability and configurability, we recommend the following implementation strategy for your query collections:
* Define your query collection as an enumeration that implements `QueryAPI`.
* Define each query constant with a property name and a name for each argument (if any).
* To assist users of your queries, preface their names with a type indicator (**GET** or **UPDATE**).
* Back the query collection with a configuration that implements the `Settings API`:
  * groupId: com.nordstrom.test-automation.tools
  * artifactId: settings
  * className: com.nordstrom.automation.settings.SettingsCore
* To support execution on multiple endpoints, implement `getConnection` with sub-configurations or other dynamic data sources (e.g. - web service).

##### Query Collection Example

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
        public int getArgCount() {
            return args.length;
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
}
```

## PathUtils

The **PathUtils** `getNextPath` method provides a method to acquire the next file path in sequence for the specified base name and extension in the indicated target folder. If the target folder already contains at least one file that matches the specified base name and extension, the algorithm used to select the next path will always return a path whose index is one more than the highest index that currently exists. (If a single file with no index is found, its implied index is 1.)

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
