package com.bawnorton.autocodec;

import org.junit.jupiter.api.Test;

public class ExtendingTests extends BasicTests {
    private void extendingTest(String resourceName) {
        basicTest("class/extending/%s".formatted(resourceName), resourceName);
    }

    @Test
    public void testExtending() {
        extendingTest("PartialFields");
        extendingTest("DifferentFields");
        extendingTest("IdenticalFields");
    }
}
