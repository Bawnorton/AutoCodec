package com.bawnorton.autocodec.codec.adapter.entry;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.AnnotationNode;

public interface OptionalEntryAdapterFactory {
    OptionalEntryAdapter getAdapter(ProcessingContext context, AnnotationNode optional);
}
