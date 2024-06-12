package com.bawnorton.autocodec.codec.adapter.entry;

import com.bawnorton.autocodec.context.ProcessingContext;

public interface RequiredEntryAdapterFactory {
    RequiredEntryAdapter getAdapter(ProcessingContext context);
}
