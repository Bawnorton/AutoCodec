package com.bawnorton.autocodec;

import com.bawnorton.autocodec.processor.AutoCodecProcessor;
import com.google.common.io.Resources;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import java.io.IOException;
import java.io.InputStream;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class AutoCodecTests {
    @BeforeAll
    public static void setup() {
        System.out.println("Setting up tests");
    }

    @Test
    public void printClasspath() {
        String classpath = System.getProperty("java.class.path");
        System.out.println("Classpath: " + classpath);
    }

    private Compilation compile(String resourceName) {
        JavaFileObject testResource = JavaFileObjects.forResource(Resources.getResource(resourceName));
        return Compiler.javac()
                .withProcessors(new AutoCodecProcessor())
                .compile(testResource);
    }

    private JavaFileObject getGeneratedFile(Compilation compilation, String className) {
        return compilation
                .generatedFile(StandardLocation.CLASS_OUTPUT, className + ".class")
                .orElseThrow(() -> new AssertionError("No output file found"));
    }

    private ClassNode readClassNode(JavaFileObject output) {
        try (InputStream inputStream = output.openInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            ClassReader classReader = new ClassReader(bytes);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            return classNode;
        } catch (IOException e) {
            throw new AssertionError("Failed to read class file", e);
        } finally {
            output.delete();
        }
    }

    @Test
    public void testSimpleRecord() {
        Compilation compilation = compile("TestRecord.java");
        assertThat(compilation).succeeded();
        JavaFileObject output = getGeneratedFile(compilation, "TestRecord");
        ClassNode classNode = readClassNode(output);

        FieldNode codecField = classNode.fields.stream()
                .filter(fieldNode -> fieldNode.name.equals("CODEC"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected `CODEC` field"));
    }

    @Test
    public void testEmptyRecord() {
        Compilation compilation = compile("EmptyRecord.java");
        assertThat(compilation).succeeded();
        JavaFileObject output = getGeneratedFile(compilation, "EmptyRecord");
        ClassNode classNode = readClassNode(output);

        assert classNode.fields.stream().noneMatch(fieldNode -> fieldNode.name.equals("CODEC"));
    }

    @Test
    public void testAlternateCodec() {
        Compilation compilation = compile("AlternateCodec.java");
        assertThat(compilation).succeeded();
        JavaFileObject output = getGeneratedFile(compilation, "AlternateCodec");
        ClassNode classNode = readClassNode(output);

        FieldNode codecField = classNode.fields.stream()
                .filter(fieldNode -> fieldNode.name.equals("CODEC"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected `CODEC` field"));
    }
}
