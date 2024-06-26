package com.bawnorton.autocodec;

import com.google.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class BasicTests extends AutoCodecTestBase {
    private void basicClassTest(String resourceName) {
        basicTest("basic/classes/%s".formatted(resourceName));
    }

    private void basicRecordTest(String resourceName) {
        basicTest("basic/records/%s".formatted(resourceName));
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
        assert compileAndRead("basic/classes/Empty")
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
        Compilation compilation = compile("basic/classes/TooManyFields");
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
        assert compileAndRead("basic/records/Empty")
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
