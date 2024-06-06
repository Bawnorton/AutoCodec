package com.bawnorton.autocodec.codec;

import com.bawnorton.autocodec.node.FieldAccessNode;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.code.Type;
import java.util.HashMap;
import java.util.Map;

public final class CodecLookup {
    private static final Map<String, CodecLookupFunction> CODEC_LOOKUP = new HashMap<>();

    static {
        CODEC_LOOKUP.put("java.lang.Integer", forName("INT"));
        CODEC_LOOKUP.put("java.lang.Long", forName("LONG"));
        CODEC_LOOKUP.put("java.lang.Float", forName("FLOAT"));
        CODEC_LOOKUP.put("java.lang.Double", forName("DOUBLE"));
        CODEC_LOOKUP.put("java.lang.Boolean", forName("BOOL"));
        CODEC_LOOKUP.put("java.lang.Byte", forName("BYTE"));
        CODEC_LOOKUP.put("java.lang.Short", forName("SHORT"));
        CODEC_LOOKUP.put("java.lang.String", forName("STRING"));
        CODEC_LOOKUP.put("java.nio.ByteBuffer", forName("BYTE_BUFFER"));
        CODEC_LOOKUP.put("java.util.stream.IntStream", forName("INT_STREAM"));
        CODEC_LOOKUP.put("java.util.stream.LongStream", forName("LONG_STREAM"));
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

    private static CodecLookupFunction forName(String name) {
        return context -> FieldAccessNode.builder(context).selected("Codec").name(name).build();
    }

    public interface CodecLookupFunction {
        FieldAccessNode apply(ProcessingContext context);
    }
}
