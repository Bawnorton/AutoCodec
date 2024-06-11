package extending;

import com.bawnorton.autocodec.AutoCodec;

@AutoCodec
public class IdenticalFields extends IdenticalFieldsParent {
    private String string;
    private Integer integer;
}

class IdenticalFieldsParent {
    private String string;
    private Integer integer;
}