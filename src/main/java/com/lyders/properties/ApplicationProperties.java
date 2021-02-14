package com.lyders.properties;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lyders.properties.ApplicationProperties.PATH_TYPE.*;

/**
 * @author Richard@Lyders.com
 */
@Data
public class ApplicationProperties extends HashMap<String, String> implements Serializable {

    private static final long serialVersionUID = 240517204195025182L;

    private static final Log LOG = LogFactory.getLog(ApplicationProperties.class);

    private static final String PROPERTY_SOURCE_ENV = "env";
    private static final String PROPERTY_SOURCE_PROP = "prop";

    public enum PATH_TYPE {
        CLASSPATH_PREFIX("classpath:"),
        FILEPATH_PREFIX("file:"),
        SERVLET_PREFIX("servlet:");

        public final String value;

        PATH_TYPE(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

    }

    public static final String PATH_TYPE_REGEX = "^([^:]+:)?([^:]+)$";
    public static final String FAILED_TO_LOAD_PROPERTIES_FROM_CLASS_PATH_MSG_TEMPLATE = "Failed to load '%s' properties file named '%s' from path: %s";

    private final ApplicationPropertiesConfig cfg;

    private final String propertiesFileName;
    private final String suffixedFileName;

    private final LinkedHashMap<String, Properties> sources = new LinkedHashMap<>();

    private final HashMap<String, String> cachedProps = new HashMap<>();

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
        if (pathTypeStrs != null && pathTypeStrs.length > 0) {
            // append additional paths to local copy of the given config
            this.cfg.getPaths().addAll(Arrays.asList(pathTypeStrs));
        }

        propertiesFileName = this.cfg.getPropertiesFileName();
        suffixedFileName = this.cfg.getSuffixFileName();

        init();
    }

    void init() throws FileNotFoundException {
        loadDefaults(this.cfg);
        loadPropertiesFromPaths(this.cfg);
    }

    void loadPropertiesFromClassPath(String pathStr, String propertiesFileName) throws FileNotFoundException {
        String filePathStr = Paths.get(pathStr, propertiesFileName).toString();
        URL res = this.getClass().getClassLoader().getResource(filePathStr);
        try (InputStream in = res.openStream()) {
            Properties properties = new Properties();
            properties.load(in);
            logSourceFilePathAndProperties(res.toString(), properties);
            putAll((Map) properties);
        } catch (NullPointerException | IOException e) {
            throw new FileNotFoundException(String.format(FAILED_TO_LOAD_PROPERTIES_FROM_CLASS_PATH_MSG_TEMPLATE, PATH_TYPE.CLASSPATH_PREFIX, propertiesFileName, pathStr));
        }
    }

    void loadPropertiesFromFileSystem(String pathStr, String propertiesFileName) throws FileNotFoundException {
        String filePathStr = Paths.get(pathStr, propertiesFileName).toAbsolutePath().toString();
        try (FileReader fileReader = new FileReader(filePathStr, StandardCharsets.UTF_8)) {
            Properties properties = new Properties();
            properties.load(fileReader);
            logSourceFilePathAndProperties(filePathStr, properties);
            putAll((Map) properties);
        } catch (IOException e) {
            throw new FileNotFoundException(String.format(FAILED_TO_LOAD_PROPERTIES_FROM_CLASS_PATH_MSG_TEMPLATE, PATH_TYPE.FILEPATH_PREFIX, propertiesFileName, pathStr));
        }
    }

    void loadPropertiesFromServletPath(String pathStr, String propertiesFileName) throws FileNotFoundException {
        String filePathStr = Paths.get(cfg.getServletPropertiesBaseDirectory(), pathStr, propertiesFileName).toAbsolutePath().toString();
        try (FileReader fileReader = new FileReader(filePathStr, StandardCharsets.UTF_8)) {
            Properties properties = new Properties();
            properties.load(fileReader);
            logSourceFilePathAndProperties(filePathStr, properties);
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

                pathType = getPathTypeDefault(cfg, pathType);
                loadFromPathType(pathType, pathStr);
            }

        }
    }

    private void loadFromPathType(String pathType, String pathStr) throws FileNotFoundException {
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

    private String getPathTypeDefault(ApplicationPropertiesConfig cfg, String pathType) {
        if (StringUtils.isEmpty(pathType)) {
            if (cfg.getServletContext() != null) {
                pathType = SERVLET_PREFIX.value;
            } else {
                pathType = FILEPATH_PREFIX.value;
            }
        }
        return pathType;
    }

    /* utility method to print out a list of all the final property values
     * */
    public void printAllProperties(Consumer<String> f) {
        for (Map.Entry<String, String> entry : entrySet()) {
            f.accept(entry.getKey() + ": " + entry.getValue());
        }
    }

    /* utility method to print out a detailed list of which properties were loaded from each file
     * */
    public void printAllSourcesAndProperties(Consumer<String> f) {
        if (!cfg.isLogSourceFilePathsAndProperties()) {
            f.accept("Logging of source files paths and properties is not enabled");
        }
        if (sources.isEmpty()) {
            f.accept("No source files found");
        }
        int fileIdx = 0;
        for (Map.Entry<String, Properties> source : sources.entrySet()) {
            String fileName = source.getKey();
            f.accept(String.format("Source file %d: %s", ++fileIdx, fileName));
            Properties properties = source.getValue();
            Iterator<Object> iterator = properties.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                f.accept(String.format("    %s=%s", key, properties.get(key)));
            }
        }
    }

    private void logSourceFilePathAndProperties(String filePathStr, Properties properties) {
        if (cfg.isLogSourceFilePathsAndProperties()) {
            sources.put(filePathStr, properties);
        }
    }

    public String toString() {
        return String.format("propertiesFileName=%s, suffixedFileName=%s, cfg=%s, sources=%s", propertiesFileName, suffixedFileName, cfg.toString(), sources.toString());
    }

    public String get(String propertyName) throws PropertyEvaluatorException{
        return get(propertyName, null);
    }

    public String get(String propertyName, String defaultValue)throws PropertyEvaluatorException {
        return get(propertyName, defaultValue, true);
    }

    public String get(String propertyName, String defaultValue, boolean decodeEscapedNewlines) throws PropertyEvaluatorException {
        return get(propertyName, defaultValue, decodeEscapedNewlines, true);
    }

    /**
     * return the value of a defined property
     *
     * @param propertyName          the name of the property to return the value of
     * @param defaultValue          the value to return if the value for the property is empty
     * @param decodeEscapedNewlines if true, decode escaped new line characters
     * @param eval                  if true, replace expressions in the form of "${env:mysysenvar}" or "${propr:mysysprop}" with their respective values from System.getenv("mysysenvar") or System.getProperty("mysysprop") respectively
     * @return the value of the property
     */
    public String get(String propertyName, String defaultValue, boolean decodeEscapedNewlines, boolean eval) throws PropertyEvaluatorException {
        if (cachedProps.containsKey(propertyName)) {
            return cachedProps.get(propertyName);
        }

        String propVal = super.get(propertyName);
        if (StringUtils.isEmpty(propVal)) {
            propVal = defaultValue;
        }
        if (propVal == null) {
            LOG.warn(String.format("No value found for application property: %s", propertyName));
        } else {
            if (eval) {
                propVal = evaluateExpression(propertyName, propVal);
            }
            if (decodeEscapedNewlines) {
                propVal = propVal.replace("\\n", "\n");
            }
        }
        cachedProps.put(propertyName, propVal);
        return propVal;
    }

    private String evaluateExpression(String propertyName, String propVal) throws PropertyEvaluatorException {
        Pattern pattern = Pattern.compile(String.format("\\$\\{\\w*(%s|%s)\\w*:(.*?)\\}",
                PROPERTY_SOURCE_ENV, PROPERTY_SOURCE_PROP));
        Matcher matcher = pattern.matcher(propVal);
        while (matcher.find()) {
            String propSource = matcher.group(1);
            String propSourceVarName = matcher.group(2);
            if (!StringUtils.isEmpty(propSource) && !StringUtils.isEmpty(propSourceVarName)) {
                try {
                    propVal = evaluateMatchedExpression(propVal, matcher);
                } catch (Exception e) {
                    throw new PropertyEvaluatorException(String.format("Failed to evaluate property '%s' expression '%s': %s", propertyName, matcher.group(0), e.getMessage()));
                }
            }
            matcher.reset(propVal);
        }
        return propVal;
    }

    private String evaluateMatchedExpression(String expression, Matcher matcher) {
        String placeholder = matcher.group(0);
        String propSource = matcher.group(1);
        String propSourceVarName = matcher.group(2);
        String propSourceVarValue;
        switch (propSource) {
            case PROPERTY_SOURCE_ENV:
                propSourceVarValue = System.getenv(propSourceVarName);
                break;
            case PROPERTY_SOURCE_PROP:
                propSourceVarValue = System.getProperty(propSourceVarName);
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown property source: %s", propSource));
        }
        if (propSourceVarValue == null) {
            throw new IllegalArgumentException(String.format("No value found for system %s: %s", propSource, propSourceVarName));
        }
        expression = expression.replace(placeholder, propSourceVarValue);
        if (StringUtils.isEmpty(expression)) {
            LOG.warn(String.format("Missing value for environment variable '%s'", propSourceVarName));
        }
        return expression;
    }

    public Long getLong(String propertyName) throws PropertyEvaluatorException{
        return getLong(propertyName, null);
    }

    public Long getLong(String propertyName, String defaultValue) throws PropertyEvaluatorException{
        return getLong(propertyName, defaultValue, true);
    }

    public Long getLong(String propertyName, String defaultValue, boolean eval) throws PropertyEvaluatorException{
        Long val = null;
        String longStr = get(propertyName, defaultValue, eval);
        if (!StringUtils.isEmpty(longStr)) {
            try {
                val = Long.valueOf(longStr);
            } catch (NumberFormatException e) {
                LOG.warn(String.format("Failed to convert '%s' to Long, so trying default '%s': %s", longStr, defaultValue, e.getMessage()));
                try {
                    val = Long.valueOf(defaultValue);
                } catch (NumberFormatException e2) {
                    LOG.warn(String.format("Failed to convert default value '%s' to Long: %s", defaultValue, e.getMessage()));
                }
            }
        }
        return val;
    }

    public Integer getInteger(String propertyName) throws PropertyEvaluatorException{
        return getInteger(propertyName, null);
    }

    public Integer getInteger(String propertyName, String defaultValue) throws PropertyEvaluatorException{
        return getInteger(propertyName, defaultValue, true);
    }

    public Integer getInteger(String propertyName, String defaultValue, boolean eval) throws PropertyEvaluatorException{
        Integer val = null;
        String valStr = get(propertyName, defaultValue, eval);
        if (!StringUtils.isEmpty(valStr)) {
            try {
                val = Integer.valueOf(valStr);
            } catch (NumberFormatException e) {
                LOG.warn(String.format("Failed to convert '%s' to Integer, so trying default '%s': %s", valStr, defaultValue, e.getMessage()));
                try {
                    val = Integer.valueOf(defaultValue);
                } catch (NumberFormatException e2) {
                    LOG.warn(String.format("Failed to convert default value '%s' to Integer: %s", defaultValue, e.getMessage()));
                }
            }
        }
        return val;
    }

    public Boolean getBoolean(String propertyName) throws PropertyEvaluatorException{
        return getBoolean(propertyName, "");
    }

    public Boolean getBoolean(String propertyName, String defaultValue) throws PropertyEvaluatorException{
        return getBoolean(propertyName, defaultValue, true);
    }

    public Boolean getBoolean(String propertyName, String defaultValue, boolean eval) throws PropertyEvaluatorException{
        return Boolean.valueOf(get(propertyName, defaultValue, eval));
    }

}
