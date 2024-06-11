package com.bawnorton.autocodec;

import org.junit.jupiter.api.Test;

public class OptionalEntryTests extends AutoCodecTestBase {
    private void optionalTest(String resourceName) {
        basicTest("optional/%s".formatted(resourceName));
    }

    @Test
    public void testMethodResolver() {
        optionalTest("MethodResolver");
    }

    @Test
    public void testExpressionResolver() {
        optionalTest("ExpressionResolver");
    }
}
