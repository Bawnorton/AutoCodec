package com.bawnorton.autocodec;

import com.google.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class BasicTests extends AutoCodecTestBase {
    private void basicClassTest(String resourceName) {
        basicTest("class/basic/%s".formatted(resourceName), resourceName);
    }

    private void basicRecordTest(String resourceName) {
        basicTest("record/basic/%s".formatted(resourceName), resourceName);
    }

    @Test
    public void testAllTypes() {
        basicClassTest("AllTypes");
    }

    @Test
    public void testAlternateCodec() {
        basicClassTest("AlternateCodec");
    }

    @Test
    public void testEmpty() {
        assert compileAndRead("class/basic/Empty", "Empty")
                .fields.stream().noneMatch(fieldNode -> fieldNode.name.equals("CODEC"));
    }

    @Test
    public void testExistingCtorDifOrder() {
        basicClassTest("ExistingCtorDifOrder");
    }

    @Test
    public void testLotOfStrings() {
        basicClassTest("LotOfStrings");
    }

    @Test
    public void testSimple() {
        basicClassTest("Simple");
    }

    @Test
    public void testTooManyFields() {
        Compilation compilation = compile("class/basic/TooManyFields");
        assertThat(compilation).failed();
    }

    @Test
    public void testAllTypesRecord() {
        basicRecordTest("AllTypes");
    }

    @Test
    public void testAlternateCodecRecord() {
        basicRecordTest("AlternateCodec");
    }

    @Test
    public void testEmptyRecord() {
        assert compileAndRead("record/basic/Empty", "Empty")
                .fields.stream().noneMatch(fieldNode -> fieldNode.name.equals("CODEC"));
    }

    @Test
    public void testLotOfStringsRecord() {
        basicRecordTest("LotOfStrings");
    }

    @Test
    public void testSimpleRecord() {
        basicRecordTest("Simple");
    }
}
