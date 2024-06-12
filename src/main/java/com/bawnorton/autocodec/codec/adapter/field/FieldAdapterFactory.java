package com.bawnorton.autocodec.codec.adapter.field;

import com.bawnorton.autocodec.context.ProcessingContext;

public interface FieldAdapterFactory {
    FieldAdpater getAdapter(ProcessingContext context);
}
