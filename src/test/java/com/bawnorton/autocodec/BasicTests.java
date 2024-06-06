package com.bawnorton.autocodec;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

public class BasicTests extends AutoCodecTestBase {
    protected void basicTest(String resourceName, String outputName) {
        ClassNode classNode = compileAndRead(resourceName, outputName);
        classNode.fields.stream()
                .filter(fieldNode -> fieldNode.name.equals("CODEC"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected `CODEC` field"));
    }

    private void basicClassTest(String resourceName) {
        basicTest("class/basic/%s".formatted(resourceName), resourceName);
    }

    private void basicRecordTest(String resourceName) {
        basicTest("record/basic/%s".formatted(resourceName), resourceName);
    }

    @Test
    public void testBasicClasses() {
        basicClassTest("AllTypes");
        basicClassTest("AlternateCodec");

        assert compileAndRead("class/basic/Empty", "Empty")
                .fields.stream().noneMatch(fieldNode -> fieldNode.name.equals("CODEC"));

        basicClassTest("LotOfStrings");
        basicClassTest("Simple");
    }

    @Test
    public void testBasicRecords() {
        basicRecordTest("AllTypes");
        basicRecordTest("AlternateCodec");

        assert compileAndRead("class/basic/Empty", "Empty")
                .fields.stream().noneMatch(fieldNode -> fieldNode.name.equals("CODEC"));

        basicRecordTest("LotOfStrings");
        basicRecordTest("Simple");
    }
}
