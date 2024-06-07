package com.bawnorton.autocodec;

import org.junit.jupiter.api.Test;

public class CollectionTests extends AutoCodecTestBase {
    private void collectionTest(String resourceName) {
        basicTest("collection/%s".formatted(resourceName), resourceName);
    }

    @Test
    public void testSingleList() {
        collectionTest("SingleList");
    }

    @Test
    public void testListChild() {
        collectionTest("ListChild");
    }
}