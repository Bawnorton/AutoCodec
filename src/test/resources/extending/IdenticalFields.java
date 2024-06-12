package extending;

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.IncludeInChildren;

@AutoCodec
public class IdenticalFields extends IdenticalFieldsParent {
    private String string;
    private Integer integer;
}

class IdenticalFieldsParent {
    @IncludeInChildren
    protected String string;
    @IncludeInChildren
    protected Integer integer;
}