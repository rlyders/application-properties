package com.lyders.properties;

import lombok.Data;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletContext;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Richard@Lyders.com
 */
@Data
public class ApplicationPropertiesConfig implements Serializable {

    private static final long serialVersionUID = 240517204195025181L;

    public enum LoadClassPathRootPropertiesAsDefaults {
        YES,
        NO
    }

    public enum LogSourceFilePathsAndProperties {
        YES,
        NO
    }

    static final String DEFAULT_PROPERTIES_BASE_FILENAME = "application";
    static final String DEFAULT_PROPERTIES_EXTENSION = ".properties";
    static final String DEFAULT_PROPERTIES_FILENAME = DEFAULT_PROPERTIES_BASE_FILENAME + DEFAULT_PROPERTIES_EXTENSION;

    public static final String CATALINA_BASE = "catalina.base";
    public static final String CATALINA_COMMON = "catalina.common";

    public static final String DEFAULT_SERVLET_PROPERTIES_FILE_EXT = ".conf";
    public static final String DEFAULT_SERVLET_PROPERTIES_PARENT_DIR = "conf";
    public static final String DEFAULT_SERVLET_PROPERTIES_SUB_DIR = "apps";

    private final String propertiesFileName;
    private final String overrideSuffix;
    private final LoadClassPathRootPropertiesAsDefaults loadClassPathRootPropertiesAsDefaults;
    private final LogSourceFilePathsAndProperties logSourceFilePathsAndProperties;
    private final ArrayList<String> paths = new ArrayList<>();
    private final transient ServletContext servletContext;
    private final String servletPropertiesBaseDirectory;

    /* construct a new instance with all default values
     * */
    public ApplicationPropertiesConfig() {
        servletContext = null;
        servletPropertiesBaseDirectory = null;
        this.propertiesFileName = null;
        this.overrideSuffix = null;
        this.loadClassPathRootPropertiesAsDefaults = LoadClassPathRootPropertiesAsDefaults.YES;
        this.logSourceFilePathsAndProperties = LogSourceFilePathsAndProperties.NO;
    }

    /* construct a new instance based on all field values given as parameters: overloaded with defaults to not require LogSourceFilePathsAndProperties
     * */
    public ApplicationPropertiesConfig(String propertiesFileName, String overrideSuffix, LoadClassPathRootPropertiesAsDefaults loadClassPathRootPropertiesAsDefaults, String... additionalPaths) {
        this(propertiesFileName, overrideSuffix, loadClassPathRootPropertiesAsDefaults, LogSourceFilePathsAndProperties.NO, additionalPaths);
    }

    /* construct a new instance based on all field values given as parameters: overloaded with defaults to not require LoadClassPathRootPropertiesAsDefaults or LogSourceFilePathsAndProperties
     * */
    public ApplicationPropertiesConfig(String propertiesFileName, String overrideSuffix, String... additionalPaths) {
        this(propertiesFileName, overrideSuffix, LoadClassPathRootPropertiesAsDefaults.YES, LogSourceFilePathsAndProperties.NO, additionalPaths);
    }

    /* construct a new instance based on all field values given as parameters
     * */
    public ApplicationPropertiesConfig(String propertiesFileName, String overrideSuffix,
                                       boolean loadClassPathRootPropertiesAsDefaults, boolean logSourceFilePathsAndProperties,
                                       String... additionalPaths) {
        this(propertiesFileName, overrideSuffix,
                loadClassPathRootPropertiesAsDefaults ? LoadClassPathRootPropertiesAsDefaults.YES : LoadClassPathRootPropertiesAsDefaults.NO,
                logSourceFilePathsAndProperties ? LogSourceFilePathsAndProperties.YES : LogSourceFilePathsAndProperties.NO,
                additionalPaths);
    }

    /* construct a new instance based on all field values given as parameters
     * */
    public ApplicationPropertiesConfig(String propertiesFileName, String overrideSuffix, LoadClassPathRootPropertiesAsDefaults loadClassPathRootPropertiesAsDefaults, LogSourceFilePathsAndProperties logSourceFilePathsAndProperties, String... additionalPaths) {
        servletContext = null;
        servletPropertiesBaseDirectory = null;
        this.propertiesFileName = propertiesFileName;
        this.overrideSuffix = overrideSuffix;
        this.loadClassPathRootPropertiesAsDefaults = loadClassPathRootPropertiesAsDefaults;
        this.logSourceFilePathsAndProperties = logSourceFilePathsAndProperties;
        if (additionalPaths != null) {
            paths.addAll(Arrays.asList(additionalPaths));
        }
    }

    /* construct a new instance by copying all the values from the given existing instance and appending the optional additional paths
     * */
    public ApplicationPropertiesConfig(ApplicationPropertiesConfig cfg, String... additionalPaths) {
        servletContext = null;
        servletPropertiesBaseDirectory = null;
        propertiesFileName = cfg.getPropertiesFileName();
        overrideSuffix = cfg.getOverrideSuffix();
        loadClassPathRootPropertiesAsDefaults = cfg.getLoadClassPathRootPropertiesAsDefaults();
        this.logSourceFilePathsAndProperties = cfg.getLogSourceFilePathsAndProperties();
        if (cfg.getPaths() != null) {
            paths.addAll(cfg.getPaths());
        }
        if (additionalPaths != null) {
            paths.addAll(Arrays.asList(additionalPaths));
        }
    }

    /* construct a new instance based on a server container context in order to load properties files from a JDNI context: overloaded with defaults to not require LoadClassPathRootPropertiesAsDefaults
     * */
    public ApplicationPropertiesConfig(ServletContext servletContext, String propertiesFileName, String overrideSuffix, LogSourceFilePathsAndProperties logSourceFilePathsAndProperties, String... additionalPaths) {
        this(servletContext, propertiesFileName, overrideSuffix, LoadClassPathRootPropertiesAsDefaults.YES, logSourceFilePathsAndProperties, additionalPaths);
    }

    /* construct a new instance based on a server container context in order to load properties files from a JDNI context: overloaded with defaults to not require LogSourceFilePathsAndProperties
     * */
    public ApplicationPropertiesConfig(ServletContext servletContext, String propertiesFileName, String overrideSuffix, LoadClassPathRootPropertiesAsDefaults loadClassPathRootPropertiesAsDefaults, String... additionalPaths) {
        this(servletContext, propertiesFileName, overrideSuffix, loadClassPathRootPropertiesAsDefaults, LogSourceFilePathsAndProperties.NO, additionalPaths);
    }

    /* construct a new instance based on a server container context in order to load properties files from a JDNI context: overloaded with defaults to not require LoadClassPathRootPropertiesAsDefaults or LogSourceFilePathsAndProperties
     * */
    public ApplicationPropertiesConfig(ServletContext servletContext, String propertiesFileName, String overrideSuffix, String... additionalPaths) {
        this(servletContext, propertiesFileName, overrideSuffix, LoadClassPathRootPropertiesAsDefaults.YES, LogSourceFilePathsAndProperties.NO, additionalPaths);
    }

    /* construct a new instance based on a server container context in order to load properties files from a JDNI context: overloaded with defaults to not require any additional params but ServletContext
     * */
    public ApplicationPropertiesConfig(ServletContext servletContext) {
        this(servletContext, null, null, LoadClassPathRootPropertiesAsDefaults.YES,
                LogSourceFilePathsAndProperties.NO, (String) null);
    }

    /* construct a new instance based on a server container context in order to load properties files from a JDNI context
     * */
    public ApplicationPropertiesConfig(ServletContext servletContext, String propertiesFileName, String overrideSuffix, LoadClassPathRootPropertiesAsDefaults loadClassPathRootPropertiesAsDefaults, LogSourceFilePathsAndProperties logSourceFilePathsAndProperties, String... additionalPaths) {
        this.servletContext = servletContext;
        servletPropertiesBaseDirectory = getServletPropertiesBaseDirectory();
        if (this.servletContext == null) {
            throw new IllegalStateException("Given ServletContext must not be null");
        }
        if (!StringUtils.isEmpty(propertiesFileName)) {
            this.propertiesFileName = propertiesFileName;
        } else {
            this.propertiesFileName = getServletContextPath().replaceAll("^/", "") + DEFAULT_SERVLET_PROPERTIES_FILE_EXT;
        }

        this.overrideSuffix = overrideSuffix;
        this.loadClassPathRootPropertiesAsDefaults = loadClassPathRootPropertiesAsDefaults;
        this.logSourceFilePathsAndProperties = logSourceFilePathsAndProperties;
        if (additionalPaths != null && additionalPaths.length > 0) {
            paths.addAll(Arrays.asList(additionalPaths));
        } else {
            paths.add(getServletDefaultPropertiesFilePath());
        }
    }

    String getServletDefaultPropertiesFilePath() {
        return ApplicationProperties.PATH_TYPE.SERVLET_PREFIX + Paths.get(DEFAULT_SERVLET_PROPERTIES_PARENT_DIR, DEFAULT_SERVLET_PROPERTIES_SUB_DIR, getServletContextPath()).toString();
    }

    public String getSuffixFileName() {
        String suffixedFileName = null;
        if (!StringUtils.isEmpty(overrideSuffix)) {
            String baseFileName = FilenameUtils.getBaseName(propertiesFileName);
            String fileNameExtension = FilenameUtils.getExtension(propertiesFileName);
            suffixedFileName = baseFileName + overrideSuffix + '.' + fileNameExtension;
        }
        return suffixedFileName;
    }

    public String getPropertiesFileName() {
        if (StringUtils.isEmpty(propertiesFileName)) {
            return DEFAULT_PROPERTIES_FILENAME;
        } else {
            return propertiesFileName;
        }
    }

    public String getServletContextName() {
        if (this.servletContext == null) {
            throw new IllegalStateException("ServletContext must not be null to get the context name");
        }
        return servletContext.getServletContextName();
    }

    public String getServletContextPath() {
        if (this.servletContext == null) {
            throw new IllegalStateException("ServletContext must not be null to get the context path");
        }
        return servletContext.getContextPath();
    }

    public String getServletPropertiesBaseDirectory() {
        String catalinaBase = System.getProperty(CATALINA_COMMON);
        if (StringUtils.isEmpty(catalinaBase)) {
            catalinaBase = System.getProperty(CATALINA_BASE);
            if (StringUtils.isEmpty(catalinaBase)) {
                throw new IllegalStateException("Failed to find CATALINA_COMMON or CATALINA_BASE system properties.");
            }
        }
        return catalinaBase;
    }

    boolean isLoadClassPathRootPropertiesAsDefaults() {
        return loadClassPathRootPropertiesAsDefaults.equals(LoadClassPathRootPropertiesAsDefaults.YES);
    }

    boolean isLogSourceFilePathsAndProperties() {
        return logSourceFilePathsAndProperties.equals(LogSourceFilePathsAndProperties.YES);
    }

    public String toString() {
        return String.format("propertiesFileName=%s, overrideSuffix=%s, loadClassPathRootPropertiesAsDefaults=%s, logSourceFilePathsAndProperties=%s, paths=%s, servletContext=%s, servletPropertiesBaseDirectory=%s",
                propertiesFileName, overrideSuffix, loadClassPathRootPropertiesAsDefaults, logSourceFilePathsAndProperties, paths.toString(), servletContext, servletPropertiesBaseDirectory);
    }

}
