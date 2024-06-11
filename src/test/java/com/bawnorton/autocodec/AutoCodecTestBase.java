package com.bawnorton.autocodec;

import com.bawnorton.autocodec.processor.AutoCodecProcessor;
import com.google.common.io.Resources;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import java.io.IOException;
import java.io.InputStream;

import static com.google.testing.compile.CompilationSubject.assertThat;

public abstract class AutoCodecTestBase {
    protected Compilation compile(String resourceName) {
        JavaFileObject testResource = JavaFileObjects.forResource(Resources.getResource("%s.java".formatted(resourceName)));
        Compilation compilation = Compiler.javac()
                .withProcessors(new AutoCodecProcessor())
                .compile(testResource);

        compilation.notes().forEach(System.err::println);
        compilation.warnings().forEach(System.err::println);
        compilation.errors().forEach(System.err::println);

        return compilation;
    }

    protected JavaFileObject getGeneratedFile(Compilation compilation, String resourceName) {
        return compilation
                .generatedFile(StandardLocation.CLASS_OUTPUT, "%s.class".formatted(resourceName))
                .orElseThrow(() -> new AssertionError("No output file found"));
    }

    protected ClassNode readClassNode(JavaFileObject output) {
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

    protected ClassNode compileAndRead(String resourceName) {
        Compilation compilation = compile(resourceName);
        assertThat(compilation).succeeded();
        JavaFileObject output = getGeneratedFile(compilation, resourceName);
        return readClassNode(output);
    }

    protected void basicTest(String resourceName) {
        ClassNode classNode = compileAndRead(resourceName);
        classNode.fields.stream()
                .filter(fieldNode -> fieldNode.name.equals("CODEC"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected `CODEC` field"));
    }
}
