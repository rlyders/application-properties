package com.lyders.properties;

import org.junit.jupiter.api.Test;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.sound.midi.SysexMessage;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lyders.properties.ApplicationProperties.*;
import static com.lyders.properties.ApplicationProperties.PATH_TYPE.SERVLET_PREFIX;
import static com.lyders.properties.ApplicationPropertiesConfig.*;
import static org.junit.jupiter.api.Assertions.*;

public class ApplicationPropertiesTests {

    /*
    test that the following default-named properties files are successfully loaded:
      * from the classpath
        * application.properties
    * */
    @Test
    public void LoadFromTestResources() throws FileNotFoundException, PropertyEvaluatorException {
        ApplicationProperties properties = new ApplicationProperties(null, null);
//        properties.printAllEntries(System.out);

        // test that the property named "test" is properly loaded with the correct value from the default properties file.
        assertEquals("classpath", properties.get("test"));
        // test that the file-specific test property named "classpath-application-properties" is properly loaded with the correct value from the default properties file.
        assertEquals("classpath:application.properties", properties.get("classpath-application-properties"));
    }

    /*
    test that the following default-named properties files are successfully loaded:
      * from the classpath
        * application.properties
        * application-unittest.properties
    * */
    @Test
    public void LoadFromTestResourcesPlusSuffix() throws FileNotFoundException, PropertyEvaluatorException {
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(null, "-unittest", LoadClassPathRootPropertiesAsDefaults.YES);
        ApplicationProperties properties = new ApplicationProperties(cfg);
//        properties.printAllEntries(System.out);

        // test that the test property named "test" property is properly loaded with the correct value from the default properties file.
        assertEquals("classpath unittest suffix", properties.get("test"));
        // test that the file-specific test property named "classpath-application-properties" from the default properties file is properly loaded with the correct value from the default properties file.
        assertEquals("classpath:application.properties", properties.get("classpath-application-properties"));
        // test that the file-specific test property named "classpath-unittest-application-properties" from the *suffixed* default properties file is properly loaded with the correct value from the default properties file.
        assertEquals("classpath:application-unittest.properties", properties.get("classpath-unittest-application-properties"));
    }

    /*
    test that the following default-named properties file is successfully loaded:
      * from the "conf" directory in the file system's current working directory
        * application.properties
    * */
    @Test
    public void LoadFromFileSystem() throws FileNotFoundException, PropertyEvaluatorException {
        // System.out.println("working directory: "+System.getProperty("user.dir"));
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(null, null, LoadClassPathRootPropertiesAsDefaults.NO);
        ApplicationProperties properties = new ApplicationProperties(cfg, "conf");
//        properties.printAllEntries(System.out);

        // test that the file-specific test property named "classpath-application-properties" from the default properties file in the classpath is *NOT* loaded since loadClassPathRootPropertiesAsDefaults=NO.
        assertNull(properties.get("classpath-application-properties"));
        //  test that the test property named "test" is properly loaded with the correct value from the properties file in the "conf" directory.
        assertEquals("conf directory", properties.get("test"));
        //  test that the file-specific test property named "conf-dir-application-properties"" is properly loaded with the correct value from the properties file in the "conf" directory.
        assertEquals("conf-dir-application.properties", properties.get("conf-dir-application-properties"));
    }

    /*
    test that the following custom-named properties files are successfully loaded:
      * from the "conf" directory in the file system's current working directory
        a) application.properties
        b) application-unitest.properties
    * */
    @Test
    public void LoadFromFileSystemWithUnitTestSuffix() throws FileNotFoundException, PropertyEvaluatorException {
        // System.out.println("working directory: "+System.getProperty("user.dir"));
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(null, "-unittest", LoadClassPathRootPropertiesAsDefaults.NO);
        ApplicationProperties properties = new ApplicationProperties(cfg, "conf");
//        properties.printAllEntries(System.out);

        // test that the file-specific test property named "classpath-application-properties" from the default properties file in the classpath is *NOT* loaded since loadClassPathRootPropertiesAsDefaults=NO.
        assertNull(properties.get("classpath-application-properties"));

        // test that the test property named "test" is properly loaded with the correct value from the properties file in the "conf" directory.
        assertEquals("conf directory unittest suffix", properties.get("test"));

        // test that the file-specific test property named "conf-dir-application-properties"" is properly loaded with the correct value from the properties file in the "conf" directory.
        assertEquals("conf-dir-application.properties", properties.get("conf-dir-application-properties"));

        // test that the file-specific *suffixed* test property named "conf-dir-application-unittest-properties"" is properly loaded with the correct value from the *suffixed* properties file in the "conf" directory.
        assertEquals("conf-dir-application-unittest.properties", properties.get("conf-dir-application-unittest-properties"));
    }

    /*
    test that the following custom-named properties files are successfully loaded:
      1) from the classpath (e.g. src/test/resources)
        a) myapp.properties
        b) myapp-unittest.properties
      2) from the "conf" directory in the file system's current working directory
        a) myapp.properties
        b) myapp-unittest.properties
    * */
    @Test
    public void LoadFromCustomNameFileSystemWithUnitTestSuffix() throws FileNotFoundException, PropertyEvaluatorException {
        // System.out.println("working directory: "+System.getProperty("user.dir"));
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig("myapp.properties", "-unittest", LoadClassPathRootPropertiesAsDefaults.YES);
        ApplicationProperties properties = new ApplicationProperties(cfg, "conf");
        // properties.printAllEntries(System.out);

        // test that the file-specific test property is loaded with the correct value from the custom-named properties file in the classpath.
        assertEquals("classpath-myapp.properties", properties.get("classpath-myapp-properties"));
        // test that the file-specific test property is loaded with the correct value from the *suffixed* custom-named properties file in the classpath.
        assertEquals("classpath-myapp-unittest.properties", properties.get("classpath-myapp-unittest-properties"));

        // test that the file-specific test property is loaded with the correct value from the custom-named properties file in the "conf" directory.
        assertEquals("conf-dir-myapp.properties", properties.get("conf-dir-myapp-properties"));
        // test that the *suffixed* file-specific test property is loaded with the correct value from the *suffixed* custom-named properties file in the "conf" directory.
        assertEquals("conf-dir-myapp-unittest.properties", properties.get("conf-dir-myapp-unittest-properties"));

        // test that the test property is loaded with the correct value from the final overriding *suffixed* custom-named properties file in the "conf" directory.
        assertEquals("myapp conf directory unittest suffix", properties.get("test"));
    }

    /*
        test that an exception is thrown when the *suffixed* custom-named properties file is missing from the classpath when suffix is given and loadClassPathRootPropertiesAsDefaults=YES.
    * */
    @Test
    public void LoadFromCustomNameFileSystemWithUnitTestSuffixButFailBecauseOfAttemptToLoadDefaultThatDoesntExist() {
        // System.out.println("working directory: "+System.getProperty("user.dir"));
        String propBaseFileName = "myapp";
        String propFileName = "myapp" + DEFAULT_PROPERTIES_EXTENSION;
        String suffix = "-conf";
        String path = "conf";
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(propFileName, suffix, LoadClassPathRootPropertiesAsDefaults.YES);

        Exception exception = assertThrows(FileNotFoundException.class, () -> {
            ApplicationProperties properties = new ApplicationProperties(cfg, path);
        });

        String expectedMessage2 = String.format(FAILED_TO_LOAD_PROPERTIES_FROM_CLASS_PATH_MSG_TEMPLATE, PATH_TYPE.CLASSPATH_PREFIX, propBaseFileName + suffix + DEFAULT_PROPERTIES_EXTENSION, "");
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage2));
    }

    /*
        test that an exception is thrown when no properties files can be loaded because loadClassPathRootPropertiesAsDefaults=NO and no file paths were given to search.
    * */
    @Test
    public void tryToLoadPropertiesWhenNoneExists() {
        String gibberish = "gibberish";
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(gibberish, null, LoadClassPathRootPropertiesAsDefaults.NO);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            ApplicationProperties properties = new ApplicationProperties(cfg, null);
        });

        String expectedMessage2 = "Failed to find any properties files because LoadClassPathRootPropertiesAsDefaults=NO and no file paths were given to search through";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage2));
    }


    /*
        test that an exception is thrown when the default-named properties file is missing from the given custom directory in the classpath when loadClassPathRootPropertiesAsDefaults=YES.
    * */
    @Test
    public void tryToLoadPropertiesFromFileSystemThatDoesNotExist() {
        String gibberish = "gibberish";
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(null, null, LoadClassPathRootPropertiesAsDefaults.YES, gibberish);

        Exception exception = assertThrows(FileNotFoundException.class, () -> {
            ApplicationProperties properties = new ApplicationProperties(cfg, null);
        });

        String expectedMessage2 = String.format(FAILED_TO_LOAD_PROPERTIES_FROM_CLASS_PATH_MSG_TEMPLATE, ApplicationProperties.PATH_TYPE.FILEPATH_PREFIX, DEFAULT_PROPERTIES_FILENAME, gibberish);
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage2));
    }

    /*
        test that an exception is thrown when the custom-named properties file is missing from the classpath when loadClassPathRootPropertiesAsDefaults=NO.
    * */
    @Test
    public void tryToLoadPropertiesFromClassPathThatDoesNotExist() {
        String gibberish = "gibberish";
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(gibberish + ".properties", "-" + gibberish, LoadClassPathRootPropertiesAsDefaults.NO, PATH_TYPE.CLASSPATH_PREFIX + gibberish);

        Exception exception = assertThrows(FileNotFoundException.class, () -> {
            ApplicationProperties properties = new ApplicationProperties(cfg, null);
        });

        String expectedMessage2 = String.format(FAILED_TO_LOAD_PROPERTIES_FROM_CLASS_PATH_MSG_TEMPLATE, PATH_TYPE.CLASSPATH_PREFIX.value, "gibberish.properties", "gibberish");
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage2));

    }

    /*
        test the regular expression used to split the "classpath:" prefix and directory.
    * */
    @Test
    public void testRegExWithClassPath() {
        Pattern pathTypePattern = Pattern.compile(PATH_TYPE_REGEX);
        String myPath = "gibberishClassPath";
        String pathStr = PATH_TYPE.CLASSPATH_PREFIX.value + myPath;
        Matcher matcher = pathTypePattern.matcher(pathStr);
        assertTrue(matcher.find());
        assertEquals(myPath, matcher.group(2));
    }

    /*
        test the regular expression used to split the "file:" prefix and directory.
    * */
    @Test
    public void testRegExWithFilePath() {
        Pattern pathTypePattern = Pattern.compile(PATH_TYPE_REGEX);
        String myPath = "gibberishFilePath";
        String pathStr = PATH_TYPE.FILEPATH_PREFIX.value + myPath;
        Matcher matcher = pathTypePattern.matcher(pathStr);
        assertTrue(matcher.find());
        assertEquals(myPath, matcher.group(2));
    }

    /*
        test the regular expression used to split the "servlet:" prefix and directory.
    * */
    @Test
    public void testRegExWithServletPath() {
        Pattern pathTypePattern = Pattern.compile(PATH_TYPE_REGEX);
        String myPath = "gibberishServletPath";
        String pathStr = SERVLET_PREFIX.value + myPath;
        Matcher matcher = pathTypePattern.matcher(pathStr);
        assertTrue(matcher.find());
        assertEquals(myPath, matcher.group(2));
    }

    @Test
    // not really a unit test, but just a visual helper to show all what properties were loaded to each properties file
    public void testPrintAllSourcesAndEntries() throws FileNotFoundException {
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig("myapp.properties", "-unittest", LoadClassPathRootPropertiesAsDefaults.YES);
        ApplicationProperties properties = new ApplicationProperties(cfg, "conf");
        System.out.println("...................");
        System.out.println("Properties per file");
        System.out.println("...................");
        properties.printAllSourcesAndProperties(System.out::println);

        System.out.println("...................");
        System.out.println("Final property values");
        System.out.println("...................");
        properties.printAllProperties(System.out::println);
    }

    @Test
    public void checkSourcesAreNotLogged() throws FileNotFoundException {
        ApplicationProperties properties = new ApplicationProperties();
        assertEquals(0, properties.getSources().size());
    }

    @Test
    public void checkSourcesAreLogged() throws FileNotFoundException {
        String propFile = "myapp.properties";
        String propPath = "conf";
        String propFileSuffix = "-unittest";
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(propFile, propFileSuffix, LoadClassPathRootPropertiesAsDefaults.YES, LogSourceFilePathsAndProperties.YES);
        ApplicationProperties properties = new ApplicationProperties(cfg, propPath);
        String userDir = System.getProperty("user.dir");
        LinkedHashMap<String, Properties> sources = properties.getSources();

        String propFilePath = Paths.get(userDir, propPath, propFile).toString();
        assertTrue(sources.containsKey(propFilePath));

        String suffixedPropFilePath = Paths.get(userDir, propPath, cfg.getSuffixFileName()).toString();
        assertTrue(sources.containsKey(suffixedPropFilePath));
    }

    @Test
    public void printApplicationPropertiesToString() throws FileNotFoundException {
        String propFile = "myapp.properties";
        String propPath = "conf";
        String propFileSuffix = "-unittest";
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(propFile, propFileSuffix, LoadClassPathRootPropertiesAsDefaults.YES, LogSourceFilePathsAndProperties.YES);
        System.out.println("cfg.toString=" + cfg.toString());
        ApplicationProperties props = new ApplicationProperties(cfg, propPath);
        System.out.println("props.toString=" + props.toString());
    }

    @Test
    public void servletConfigDefaultPathTest() throws FileNotFoundException {
        String propFile = "my-servlet.conf";
        String servletContextName = "My Servlet";
        String servletContextPath = "/my-servlet";
        ServletContext servletContext = new MockServletContext(servletContextName, servletContextPath);
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(servletContext, propFile, null, LoadClassPathRootPropertiesAsDefaults.YES, LogSourceFilePathsAndProperties.YES);

        String expectedServletDetaultPropertiesFilePath = SERVLET_PREFIX.value + Paths.get("conf", "apps", servletContextPath);

        String actualServletDefaultPropertiesFilePath = cfg.getServletDefaultPropertiesFilePath();
        assertEquals(expectedServletDetaultPropertiesFilePath, actualServletDefaultPropertiesFilePath);

        List<String> paths = cfg.getPaths();
        String path = paths.stream().findFirst().get();
        assertEquals(expectedServletDetaultPropertiesFilePath, path);
    }

}
