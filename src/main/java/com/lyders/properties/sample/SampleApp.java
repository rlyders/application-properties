package com.lyders.properties.sample;

import com.lyders.properties.ApplicationProperties;
import com.lyders.properties.ApplicationPropertiesConfig;

import java.io.FileNotFoundException;

public class SampleApp {

    public static void main(String[] args) throws FileNotFoundException {
        SampleApp sampleApp = new SampleApp();
        sampleApp.sampleRun();
    }

    public void sampleRun() throws FileNotFoundException {
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(null, "-unittest",
                ApplicationPropertiesConfig.LoadClassPathRootPropertiesAsDefaults.NO);
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

}
