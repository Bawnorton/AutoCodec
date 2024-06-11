package com.bawnorton.autocodec.codec.entry;

import com.bawnorton.autocodec.OptionalEntry;
import com.bawnorton.autocodec.codec.clazz.CodecableClass;
import com.bawnorton.autocodec.codec.entry.factory.CodecEntryFactory;
import com.bawnorton.autocodec.codec.entry.factory.OptionalListCodecEntryFactory;
import com.bawnorton.autocodec.codec.entry.factory.OptionalMapCodecEntryFactory;
import com.bawnorton.autocodec.codec.entry.factory.OptionalNormalCodecEntryFactory;
import com.bawnorton.autocodec.codec.entry.factory.RequiredListCodecEntryFactory;
import com.bawnorton.autocodec.codec.entry.factory.RequiredMapCodecEntryFactory;
import com.bawnorton.autocodec.codec.entry.factory.RequiredNormalCodecEntryFactory;
import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.AnnotationNode;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.bawnorton.autocodec.util.FieldType;
import com.bawnorton.autocodec.util.TypeUtils;
import com.sun.tools.javac.code.Type;
import java.util.List;
import java.util.Map;

/**
 * A field in a {@link ClassDeclNode} that should be included in a codec definition.
 */
public final class CodecEntryField extends ContextHolder {
    private final ClassDeclNode enclosingClass;
    private final VariableDeclNode node;
    private final AnnotationNode optional;

    public CodecEntryField(ProcessingContext context, CodecableClass enclosingClass, VariableDeclNode node) {
        super(context);
        this.enclosingClass = enclosingClass.node();
        this.node = node;
        this.optional = node.getAnnotation(OptionalEntry.class);
    }

    public boolean isOptional() {
        return optional != null;
    }

    public VariableDeclNode node() {
        return node;
    }

    /**
     * Get the type of the parameter that will be passed to the ctor by the codec
     * <br>
     * Used for handling list/map types correctly
     */
    public Type getParameterType() {
        FieldType type = FieldType.ofField(context, node);
        return switch (type) {
            case LIST -> TypeUtils.findType(context, node.getType(), java.util.List.class);
            case MAP -> TypeUtils.findType(context, node.getType(), java.util.Map.class);
            case NORMAL -> node.getType();
        };
    }

    public CodecEntryFactory getFactory(ProcessingContext context) {
        return switch (determineEntryType()) {
            case REQUIRED_NORMAL -> new RequiredNormalCodecEntryFactory(context, enclosingClass, node);
            case OPTIONAL_NORMAL -> new OptionalNormalCodecEntryFactory(context, enclosingClass, node, optional);
            case REQUIRED_LIST -> new RequiredListCodecEntryFactory(context, enclosingClass, node);
            case OPTIONAL_LIST -> new OptionalListCodecEntryFactory(context, enclosingClass, node, optional);
            case REQUIRED_MAP -> new RequiredMapCodecEntryFactory(context, enclosingClass, node);
            case OPTIONAL_MAP -> new OptionalMapCodecEntryFactory(context, enclosingClass, node, optional);
        };
    }

    private EntryType determineEntryType() {
        FieldType type = FieldType.ofField(context, node);

        return switch (type) {
            case LIST -> isOptional() ? EntryType.OPTIONAL_LIST : EntryType.REQUIRED_LIST;
            case MAP -> isOptional() ? EntryType.OPTIONAL_MAP : EntryType.REQUIRED_MAP;
            case NORMAL -> isOptional() ? EntryType.OPTIONAL_NORMAL : EntryType.REQUIRED_NORMAL;
        };
    }

    private enum EntryType {
        REQUIRED_NORMAL,
        OPTIONAL_NORMAL,
        REQUIRED_LIST,
        OPTIONAL_LIST,
        REQUIRED_MAP,
        OPTIONAL_MAP
    }
}
