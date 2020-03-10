package com.lyders.properties;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lyders.properties.ApplicationProperties.PATH_TYPE.*;

/**
 * @author Richard@Lyders.com
 */
public class ApplicationProperties extends HashMap<String, String> {

    enum PATH_TYPE {
        CLASSPATH_PREFIX("classpath:"),
        FILEPATH_PREFIX("file:"),
        SERVLET_PREFIX("servlet:");

        public final String value;

        private PATH_TYPE(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }

    }

    public static final String PATH_TYPE_REGEX = "^([^:]+:)?([^:]+)$";
    public static final String FAILED_TO_LOAD_PROPERTIES_FROM_CLASS_PATH_MSG_TEMPLATE = "Failed to load '%s' properties file named '%s' from path: %s";

    private final ApplicationPropertiesConfig cfg;

    private final String propertiesFileName;
    private final String suffixedFileName;

    /* Overloaded constructor that passes null for config parameter of main constructor
     * */
    public ApplicationProperties() throws FileNotFoundException {
        this(null);
    }

    /**
     * Create an instance of the ApplicationProperties based on a given config and an optional list of paths used to control how property files are to be loaded
     *
     * @param cfg,          config settings to use for this instance of ApplicationProperties
     * @param pathTypeStrs, additional list of paths to append to the given list to paths in the cfg param
     * @throws FileNotFoundException if a properties file could not be found
     */
    public ApplicationProperties(ApplicationPropertiesConfig cfg, String... pathTypeStrs) throws FileNotFoundException {

        if (cfg != null) {
            // create a local copy of the given config so that we can add the additional paths without changing the given config
            this.cfg = new ApplicationPropertiesConfig(cfg);
        } else {
            this.cfg = new ApplicationPropertiesConfig();
        }
        if (pathTypeStrs != null) {
            // append additional paths to local copy of the given config
            this.cfg.getPaths().addAll(Arrays.asList(pathTypeStrs));
        }

        propertiesFileName = this.cfg.getPropertiesFileName();
        suffixedFileName = this.cfg.getSuffixFileName();

        if (pathTypeStrs != null) {
            // add the optional list of paths given via the constructor parameter
            this.cfg.getPaths().addAll(Arrays.asList(pathTypeStrs));
        }
        init();
    }

    void init() throws FileNotFoundException {
        loadDefaults(this.cfg);
        loadPropertiesFromPaths(this.cfg);
    }

    void loadPropertiesFromClassPath(String pathStr, String propertiesFileName) throws FileNotFoundException {
        String filePathStr = Paths.get(pathStr, propertiesFileName).toString();
        try {
            Properties properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream(filePathStr));
            putAll((Map) properties);
        } catch (NullPointerException | IOException e) {
            throw new FileNotFoundException(String.format(FAILED_TO_LOAD_PROPERTIES_FROM_CLASS_PATH_MSG_TEMPLATE, PATH_TYPE.CLASSPATH_PREFIX, propertiesFileName, pathStr));
        }
    }

    void loadPropertiesFromFileSystem(String pathStr, String propertiesFileName) throws FileNotFoundException {
        try (FileReader fileReader = new FileReader(Paths.get(pathStr, propertiesFileName).toString(), Charset.forName("UTF-8"))) {
            Properties properties = new Properties();
            properties.load(fileReader);
            putAll((Map) properties);
        } catch (IOException e) {
            throw new FileNotFoundException(String.format(FAILED_TO_LOAD_PROPERTIES_FROM_CLASS_PATH_MSG_TEMPLATE, PATH_TYPE.FILEPATH_PREFIX, propertiesFileName, pathStr));
        }
    }

    void loadPropertiesFromServletPath(String pathStr, String propertiesFileName) throws FileNotFoundException {
        try (FileReader fileReader = new FileReader(Paths.get(cfg.getServletPropertiesBaseDirectory(), pathStr, propertiesFileName).toString(), Charset.forName("UTF-8"))) {
            Properties properties = new Properties();

            properties.load(fileReader);
            putAll((Map) properties);
        } catch (IOException e) {
            throw new FileNotFoundException(String.format(FAILED_TO_LOAD_PROPERTIES_FROM_CLASS_PATH_MSG_TEMPLATE, PATH_TYPE.FILEPATH_PREFIX, propertiesFileName, pathStr));
        }
    }

    void loadDefaults(ApplicationPropertiesConfig cfg) throws FileNotFoundException {
        // should we try to load an application.properties file from the classpath if it exists and use it as the default set of properties?
        if (cfg.isLoadClassPathRootPropertiesAsDefaults()) {
            // load the property file from the classpath, if it exists
            loadPropertiesFromClassPath("", cfg.getPropertiesFileName());
            // additionally, if a suffixed file exists in the same place...then load it also to override the properties from the non-suffixed file
            if (cfg.getSuffixFileName() != null) {
                loadPropertiesFromClassPath("", cfg.getSuffixFileName());
            }
        }
    }

    void loadPropertiesFromPaths(ApplicationPropertiesConfig cfg) throws FileNotFoundException {
        Pattern pathTypePattern = Pattern.compile(PATH_TYPE_REGEX);

        if (!cfg.isLoadClassPathRootPropertiesAsDefaults() && cfg.getPaths().isEmpty()) {
            throw new IllegalStateException("Failed to find any properties files because LoadClassPathRootPropertiesAsDefaults=NO and no file paths were given to search through");
        }
        // load properties file from each of the given paths if a file exists there
        for (String p : cfg.getPaths()) {
            Matcher matcher = pathTypePattern.matcher(p);
            while (matcher.find()) {
                String pathType = matcher.group(1);
                if (pathType == null) {
                    pathType = FILEPATH_PREFIX.value;
                }
                String pathStr = matcher.group(2);

                if (StringUtils.isEmpty(pathType)) {
                    if (cfg.getServletContext() != null) {
                        pathType = SERVLET_PREFIX.value;
                    } else {
                        pathType = FILEPATH_PREFIX.value;
                    }
                }
                if (CLASSPATH_PREFIX.value.equals(pathType)) {
                    loadPropertiesFromClassPath(pathStr, propertiesFileName);
                    if (suffixedFileName != null) {
                        loadPropertiesFromClassPath(pathStr, suffixedFileName);
                    }
                } else if (SERVLET_PREFIX.value.equals(pathType)) {
                    loadPropertiesFromServletPath(pathStr, propertiesFileName);
                    if (suffixedFileName != null) {
                        loadPropertiesFromServletPath(pathStr, suffixedFileName);
                    }
                } else if (FILEPATH_PREFIX.value.equals(pathType)) {
                    loadPropertiesFromFileSystem(pathStr, propertiesFileName);
                    if (suffixedFileName != null) {
                        loadPropertiesFromFileSystem(pathStr, suffixedFileName);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown properties path type prefix: " + pathType);
                }
            }

        }
    }

    public void printAllEntries(Consumer<String> f) {
        for (Map.Entry entry : entrySet()) {
            f.accept(entry.getKey() + ": " + entry.getValue());
        }
    }

}
