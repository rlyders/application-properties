package com.lyders.properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/* Richard@Lyders.com created on 2/14/2021 */
class AppPropTypeTests {

    static ApplicationProperties props;

    @BeforeAll
    static public void beforeAll() {
        String propFile = "typetests.properties";
        String propPath = "conf";
        String propFileSuffix = null;
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(propFile, propFileSuffix,
                true,
                true);
        System.out.println("cfg.toString=" + cfg.toString());
        try {
            props = new ApplicationProperties(cfg, propPath);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate ApplicationProperties: " + e.getMessage());
        }
        System.out.println("props.toString=" + props.toString());
    }

    @Test
    void longTests() {
        Long testLong = props.getLong("test.long");
        assertEquals(Long.valueOf(1234567891234L), testLong);
        Long missingLong = props.getLong("missing.long");
        assertNull(missingLong);
    }

    @Test
    void integerTests() {
        Integer testInt = props.getInteger("test.int");
        assertEquals(Integer.valueOf(123), testInt);
        Integer missingInt = props.getInteger("missing.int");
        assertNull(missingInt);
    }

    @Test
    void booleanTests() {
        Boolean testBoolean = props.getBoolean("test.boolean");
        assertEquals(Boolean.valueOf("true"), testBoolean);
        Boolean missingBoolean = props.getBoolean("missing.boolean");
        assertEquals(false, missingBoolean);
    }

    @Test
    void stringTests() {
        String testString = props.get("test.string");
        assertEquals("a test string", testString);
        String missingString = props.get("missing.string");
        assertNull(missingString);
    }

    @Test
    @SetEnvironmentVariable(
            key = "test-str",
            value = "my-test-env-val")
    void stringTestsViaEnv() {
        String s = System.getenv("test-str");
        assertEquals("my-test-env-val", s);
        String testStringViaEnv = props.get("test.string-via-env");
        assertEquals("val-from-env:my-test-env-val", testStringViaEnv);
    }

    @Test
    @SetEnvironmentVariable(
            key = "my-test-env-int",
            value = "4567")
    void intTestsViaEnv() {
        String s = System.getenv("my-test-env-int");
        assertEquals("4567", s);
        Integer testIntViaEnv = props.getInteger("test.int-via-env");
        assertEquals(994567, testIntViaEnv);
    }

    @Test
    @SetEnvironmentVariable(
            key = "my-test-env-long",
            value = "5678")
    void longTestsViaEnv() {
        String s = System.getenv("my-test-env-long");
        assertEquals("5678", s);
        Long testLongViaEnv = props.getLong("test.long-via-env");
        assertEquals(885678L, testLongViaEnv);
    }

    @Test
    @SetEnvironmentVariable(
            key = "my-test-env-boolean",
            value = "t")
    void booleanTestsViaEnv() {
        String s = System.getenv("my-test-env-boolean");
        assertEquals("t", s);
        Boolean testBooleanViaEnv = props.getBoolean("test.boolean-via-env");
        assertEquals(true, testBooleanViaEnv);
    }

    @Test
    @SetSystemProperty(
            key = "test-str",
            value = "my-test-prop-val")
    void stringTestsViaProp() {
        String s = System.getProperty("test-str");
        assertEquals("my-test-prop-val", s);
        String testStringViaEnv = props.get("test.string-via-prop");
        assertEquals("val-from-prop:my-test-prop-val", testStringViaEnv);
    }

    @Test
    void stringTestsViaPropMissing() {
        String testStringViaEnv = props.get("test.string-via-prop");
        assertNull(testStringViaEnv);
    }

    @Test
    @SetSystemProperty(
            key = "my-test-prop-int",
            value = "45670")
    void intTestsViaProp() {
        String s = System.getProperty("my-test-prop-int");
        assertEquals("45670", s);
        Integer testIntViaProp = props.getInteger("test.int-via-prop");
        assertEquals(9945670, testIntViaProp);
    }

    @Test
    @SetSystemProperty(
            key = "my-test-prop-long",
            value = "56780")
    void longTestsViaProp() {
        String s = System.getProperty("my-test-prop-long");
        assertEquals("56780", s);
        Long testLongViaProp = props.getLong("test.long-via-prop");
        assertEquals(8856780L, testLongViaProp);
    }

    @Test
    @SetSystemProperty(
            key = "my-test-prop-boolean",
            value = "t")
    void booleanTestsViaProp() {
        String s = System.getProperty("my-test-prop-boolean");
        assertEquals("t", s);
        Boolean testBooleanViaProp = props.getBoolean("test.boolean-via-prop");
        assertEquals(true, testBooleanViaProp);
    }

}
