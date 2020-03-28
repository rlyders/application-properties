# ApplicationProperties

This ApplicationProperties Java package provides a fast and easily configurable application.properties loader that mimics some key features of the Spring Boot application.properties loader. This package allows for multi-level overloading of properties based on a basename and optional suffix by searching the classpath, optional configurable file paths along with the JNDI server container context.

The class [ApplicationPropertiesConfig](src/main/java/com/lyders/application-properties/ApplicationPropertiesConfig.java) controls the features of the overloading of properties such as the base name of the properties file, an optional suffix that allows for the loading of environment-specific or scenario-specific property files as needed.

The following features are supported:   
  * Names of properties files can be customized
  * Locations of properties files can be customized. The following path types are supported:
    * class path
    * file system paths
    * JNDI environment naming context (e.g., Servlet/JSP running under Tomcat)
  * Environment-specific property files can override the values in default properties files via a "suffix"
  * debug logging of all source property files loaded and the properties loaded from each. Enable this feature by passing parameter LogSourceFilePathsAndProperties.YES. See the unit test named "checkSourcesAreLogged()" for more details.
   
The overloading order of the files is controlled by a simple ordering of path parameters given to the constructor.

10+ unit tests are included with documentation to that also help to demonstrate how this class works.

## Getting Started

To get you a copy of the project up and running on your local machine for development and testing purposes, clone this github repo and run the review the unit tests source code to see the various features of the package.

### Prerequisites

  * Maven 3.6.3+
  * JDK 11.0.4+

### Installing

To install, simply add this artifact as a dependency to your existing Maven project. Add the following to your project's pom.xml:
```
<project>
    ...
    <dependencies>
        ...
        <dependency>
            <groupId>com.lyders</groupId>
            <artifactId>application-properties</artifactId>
            <version>1.0.0</version>
        </dependency>
        ...
    </dependencies>
    ...
</project>
``` 

Sample class using ApplicationProperties: [SampleApp.java](src/main/java/com/lyders/application-properties/sample/SampleApp.java)
```
public class SampleApp {
    public static void main(String[] args) throws FileNotFoundException {
        SampleApp sampleApp = new SampleApp();
        sampleApp.sampleRun();
    }
    public void sampleRun() throws FileNotFoundException {
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(null, "-unittest", LoadClassPathRootPropertiesAsDefaults.NO);
        ApplicationProperties properties = new ApplicationProperties(cfg, "conf");
        properties.printAllEntries(System.out::println);
    }
}
```
NOTES:
  * Notice that one and only one path "conf" was given to search for properties files. 
  * The parameter "loadClassPathRootPropertiesAsDefaults" was given a value of "NO" which means that the loaded will *not* try to load a file named "application.properties" from the classpath.
  * Thus, based on the given parameters, the the ApplicationPropertiesLoader will only attempt to load the following two files from the OS file system based on the current working directory:
    *  conf/application.properties
    *  conf/application-unittest.properties

Sample output based on sample properties files in this project:
  * [file:conf](conf)
  * [classpath:](src/test/resources)
```
...................
Properties per file
...................
Source file 1: C:\Users\Richa\projects\application-properties\conf\application.properties
    conf-dir-application-properties=conf-dir-application.properties
    test=conf directory
Source file 2: C:\Users\Richa\projects\application-properties\conf\application-unittest.properties
    test=conf directory unittest suffix
    conf-dir-application-unittest-properties=conf-dir-application-unittest.properties
...................
Final property values
...................
test: conf directory unittest suffix
conf-dir-application-unittest-properties: conf-dir-application-unittest.properties
conf-dir-application-properties: conf-dir-application.properties
```

NOTES: 
 * the "test" property is contained in all of the test properties files loaded for this sample app. It serves to show which properties file was loaded last and that overrides all other values. 
   * In this case, the "test" property value is "conf directory unittest suffix". 
   * If you search this project for "test=conf directory unittest suffix" you should find that [conf\application-unittest.properties](conf\application-unittest.properties) is the only properties file that contains this string. 
   * This means that this file was the last properties file loaded so that the property values defines in that file overrode any previous loaded property values.
 * The other two properties contained in the output are file-specific properties.
   * The file-specific properties are:
     * conf-dir-application-unittest-properties
     * conf-dir-application-properties
   * These two file-specific properties are not duplicated amongst the various sample property files. They only exist in one properties file. They serve to show which properties files were loaded.
 
 TIP: Always add a file-specific property to each properties file. This may help you one day as you try to to determine which files were loaded and why a property value was overloaded.  

## Running the tests

Run the automated tests for this system

```
mvn clean test
```

Sample output
```
$ mvn clean test
[INFO] Scanning for projects...
[INFO]
[INFO] -----------------< com.lyders:application-properties >------------------
[INFO] Building application-properties 1.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ application-properties ---
[INFO] Deleting C:\projects\application-properties\target
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ application-properties ---
[WARNING] Using platform encoding (Cp1252 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] Copying 0 resource
[INFO]
[INFO] --- maven-compiler-plugin:3.8.1:compile (default-compile) @ application-properties ---
[INFO] Changes detected - recompiling the module!
[WARNING] File encoding has not been set, using platform encoding Cp1252, i.e. build is platform dependent!
[INFO] Compiling 3 source files to C:\projects\application-properties\target\classes
[INFO] /C:/projects/application-properties/src/main/java/com/lyders/properties/ApplicationProperties.java: C:\projects\application-properties\src\main\java\com\lyders\properties\ApplicationProperties.java uses unchecked or unsafe operations.
[INFO] /C:/projects/application-properties/src/main/java/com/lyders/properties/ApplicationProperties.java: Recompile with -Xlint:unchecked for details.    
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ application-properties ---
[WARNING] Using platform encoding (Cp1252 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] Copying 4 resources
[INFO]
[INFO] --- maven-compiler-plugin:3.8.1:testCompile (default-testCompile) @ application-properties ---
[INFO] Changes detected - recompiling the module!
[WARNING] File encoding has not been set, using platform encoding Cp1252, i.e. build is platform dependent!
[INFO] Compiling 1 source file to C:\projects\application-properties\target\test-classes
[INFO]
[INFO] --- maven-surefire-plugin:3.0.0-M4:test (default-test) @ application-properties ---
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.lyders.properties.ApplicationPropertiesTests
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.133 s - in com.lyders.properties.ApplicationPropertiesTests
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  8.486 s
[INFO] Finished at: 2020-03-08T14:54:49-05:00
[INFO] ------------------------------------------------------------------------
```

### Unit tests

These tests validate the various configuration options available for the ApplicationProperties class. Please see the detailed unit tests and their descriptions for an understanding as to what each unit test is validating.
  * [ApplicationPropertiesTests](src/test/java/com/lyders/application-properties/ApplicationPropertiesTests.java)

```
mvn clean test
```

## Build this project

```
mvn clean package
```

## Deploy to Apache Maven (i.e., "Maven Central") via Sonatype.org

from: https://central.sonatype.org/pages/apache-maven.html

### Upload release to Sonatype staging repo. 

NOTES: 
* To avoid slowing down every build, I moved the javadoc, gpg, and source plugins under the release profile.
* I wanted to be able to validate the deployment in staging repo first, then deploy it. So, I set <autoReleaseAfterClose> to false. 
```
mvn clean deploy -P release
```

With the property autoReleaseAfterClose set to false you can manually inspect the staging repository in the Nexus Repository Manager and trigger a release of the staging repository later with
```
mvn nexus-staging:release
```

If you find something went wrong you can drop the staging repository with
```
mvn nexus-staging:drop
```

Please read Build Promotion with the Nexus Staging Suite in the book Repository Management with Nexus for more information about the Nexus Staging Maven Plugin.

... *OR* ...
  
If your version is a release version (does not end in -SNAPSHOT) and with this setup in place, you can run a deployment to OSSRH and an automated release to the Central Repository with the usual:

```
mvn release:clean release:prepare
```

by answering the prompts for versions and tags, followed by
```
mvn release:perform
```

This execution will deploy to OSSRH and release to the Central Repository in one go, thanks to the usage of the Nexus Staging Maven Plugin with autoReleaseAfterClose set to true.

## Built With

 * [Maven 3.6.3](https://maven.apache.org/) - Dependency Management
 * [Amazon Corretto Java 11.0.4](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html) 
 * [junit-jupiter 5.6.0](pom.xml)
 * IntelliJ IDEA Community Edition 2019.2.4
 * Visual Studio Code 1.42.1
 * Windows 10 Pro version 1093

## Contributing

If you find any bugs, issues, concerns or have advice on how to improve this project, please contact me at richard@lyders.com. 

## Authors

* **Richard Lyders** - [RichardLyders.com](http://richardlyders.com/)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Acknowledgments

* My little buddy Oliver, who makes each day a joy.
* Tony Benbrahim, sharing his decades of development experience
