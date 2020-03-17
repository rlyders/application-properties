package com.lyders.properties;

import lombok.Builder;
import lombok.Data;
import lombok.extern.java.Log;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletContext;
import java.nio.file.Paths;
import java.util.*;


/**
 * @author Richard@Lyders.com
 */
@Data
public class ApplicationPropertiesConfig {

    public enum LoadClassPathRootPropertiesAsDefaults {
        YES,
        NO
    }

    public enum LogSourceFilePathsAndProperties {
        YES,
        NO
    }

    final static String DEFAULT_PROPERTIES_BASE_FILENAME = "application";
    final static String DEFAULT_PROPERTIES_EXTENSION = ".properties";
    final static String DEFAULT_PROPERTIES_FILENAME = DEFAULT_PROPERTIES_BASE_FILENAME + DEFAULT_PROPERTIES_EXTENSION;

    public static final String CATALINA_BASE = "catalina.base";
    public static final String CATALINA_COMMON = "catalina.common";

    public static final String DEFAULT_SERVLET_PROPERTIES_PARENT_DIR = "conf";
    public static final String DEFAULT_SERVLET_PROPERTIES_SUB_DIR = "apps";

    private final String propertiesFileName;
    private final String overrideSuffix;
    private final LoadClassPathRootPropertiesAsDefaults loadClassPathRootPropertiesAsDefaults;
    private final HashSet<String> paths = new HashSet<String>();
    private final ServletContext servletContext;
    private final String servletPropertiesBaseDirectory;
    private String SERVLET_PARENT_DIR;

    private final LogSourceFilePathsAndProperties logSourceFilePathsAndProperties;

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
        this( servletContext, propertiesFileName, overrideSuffix, LoadClassPathRootPropertiesAsDefaults.YES, logSourceFilePathsAndProperties, additionalPaths );
    }

    /* construct a new instance based on a server container context in order to load properties files from a JDNI context: overloaded with defaults to not require LogSourceFilePathsAndProperties
     * */
    public ApplicationPropertiesConfig(ServletContext servletContext, String propertiesFileName, String overrideSuffix, LoadClassPathRootPropertiesAsDefaults loadClassPathRootPropertiesAsDefaults, String... additionalPaths) {
        this( servletContext, propertiesFileName, overrideSuffix, loadClassPathRootPropertiesAsDefaults, LogSourceFilePathsAndProperties.NO, additionalPaths );
    }

    /* construct a new instance based on a server container context in order to load properties files from a JDNI context: overloaded with defaults to not require LoadClassPathRootPropertiesAsDefaults or LogSourceFilePathsAndProperties
     * */
    public ApplicationPropertiesConfig(ServletContext servletContext, String propertiesFileName, String overrideSuffix, String... additionalPaths) {
        this( servletContext, propertiesFileName, overrideSuffix, LoadClassPathRootPropertiesAsDefaults.YES, LogSourceFilePathsAndProperties.NO, additionalPaths );
    }

    /* construct a new instance based on a server container context in order to load properties files from a JDNI context: overloaded with defaults to not require any additional params but ServletContext
     * */
    public ApplicationPropertiesConfig(ServletContext servletContext) {
        this( servletContext, null, null, LoadClassPathRootPropertiesAsDefaults.YES, LogSourceFilePathsAndProperties.NO, null);
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
            this.propertiesFileName = getServletContextName();
        }

        this.overrideSuffix = overrideSuffix;
        this.loadClassPathRootPropertiesAsDefaults = loadClassPathRootPropertiesAsDefaults;
        this.logSourceFilePathsAndProperties = logSourceFilePathsAndProperties;
        if (additionalPaths != null && additionalPaths.length > 0) {
            paths.addAll(Arrays.asList(additionalPaths));
        } else {
            paths.add(Paths.get(DEFAULT_SERVLET_PROPERTIES_PARENT_DIR, DEFAULT_SERVLET_PROPERTIES_SUB_DIR, getServletContextName()).toString());
        }
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
            throw new IllegalStateException("Given ServletContext must not be null");
        }
        return servletContext.getContextPath().substring(1);
    }

    public String getServletPropertiesBaseDirectory() {
        String servletPropertiesBaseDirectory = System.getProperty(CATALINA_COMMON);
        if (StringUtils.isEmpty(servletPropertiesBaseDirectory)) {
            servletPropertiesBaseDirectory = System.getProperty(CATALINA_BASE);
            if (StringUtils.isEmpty(servletPropertiesBaseDirectory)) {
                throw new IllegalStateException("Failed to find CATALINA_COMMON or CATALINA_BASE system properties.");
            }
        }
        return servletPropertiesBaseDirectory;
    }

    boolean isLoadClassPathRootPropertiesAsDefaults() {
        return loadClassPathRootPropertiesAsDefaults.equals(LoadClassPathRootPropertiesAsDefaults.YES);
    }

    boolean isLogSourceFilePathsAndProperties() {
        return logSourceFilePathsAndProperties.equals(LogSourceFilePathsAndProperties.YES);
    }

}
