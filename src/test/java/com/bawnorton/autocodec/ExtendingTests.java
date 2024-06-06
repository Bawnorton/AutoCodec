package com.bawnorton.autocodec;

import org.junit.jupiter.api.Test;

public class ExtendingTests extends AutoCodecTestBase {
    private void extendingTest(String resourceName) {
        basicTest("class/extending/%s".formatted(resourceName), resourceName);
    }

    @Test
    public void testExtendingDifferentFields() {
        extendingTest("DifferentFields");
    }

    @Test
    public void testExtendingExistingCtor() {
        extendingTest("ExistingCtor");
    }

    @Test
    public void testExtendingIdenticalFields() {
        extendingTest("IdenticalFields");
    }

    @Test
    public void testExtendingPartialFields() {
        extendingTest("PartialFields");
    }

    @Test
    public void testExtendingPartialIgnoredFields() {
        extendingTest("PartialIgnoredFields");
    }
}
