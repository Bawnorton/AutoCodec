package com.bawnorton.autocodec.codec;

import com.bawnorton.autocodec.node.FieldAccessNode;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.tools.javac.code.Type;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public final class CodecLookup {
    private static final Map<String, CodecLookupFunction> CODEC_LOOKUP = new HashMap<>();

    static {
        registerCodec(Integer.class, forName("INT"));
        registerCodec(Long.class, forName("LONG"));
        registerCodec(Float.class, forName("FLOAT"));
        registerCodec(Double.class, forName("DOUBLE"));
        registerCodec(Boolean.class, forName("BOOL"));
        registerCodec(Byte.class, forName("BYTE"));
        registerCodec(Short.class, forName("SHORT"));
        registerCodec(String.class, forName("STRING"));
        registerCodec(ByteBuffer.class, forName("BYTE_BUFFER"));
        registerCodec(IntStream.class, forName("INT_STREAM"));
        registerCodec(LongStream.class, forName("LONG_STREAM"));
    }

    public static FieldAccessNode lookupCodec(ProcessingContext context, Type type) {
        String typeName = type.toString();
        if (!CODEC_LOOKUP.containsKey(typeName)) {
            throw new IllegalStateException("Cannot encode type: " + typeName + " as a registered codec is not available.");
        }
        return CODEC_LOOKUP.get(type.toString()).apply(context);
    }

    public static void registerCodec(String typeName, CodecLookupFunction codecLookupFunction) {
        CODEC_LOOKUP.put(typeName, codecLookupFunction);
    }

    public static void registerCodec(Class<?> type, CodecLookupFunction codecLookupFunction) {
        registerCodec(type.getName(), codecLookupFunction);
    }

    private static CodecLookupFunction forName(String name) {
        return context -> FieldAccessNode.builder(context).selected("Codec").name(name).build();
    }

    public interface CodecLookupFunction {
        FieldAccessNode apply(ProcessingContext context);
    }
}
