package com.bawnorton.autocodec.codec.adapter.entry;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.info.AnnotationInfo;

public interface OptionalEntryAdapterFactory {
    OptionalEntryAdapter getAdapter(ProcessingContext context, AnnotationInfo optional);
}
