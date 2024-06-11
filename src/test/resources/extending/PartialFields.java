package extending;

import com.bawnorton.autocodec.AutoCodec;

@AutoCodec
public class PartialFields extends PartialFieldsParent {
    private String s1;
    private String s2;

    private PartialFields(String s1, String s2) {
        super(null, null);
        this.s1 = s1;
        this.s2 = s2;
    }
}

class PartialFieldsParent {
    private Integer i1;
    private String s1;

    public PartialFieldsParent(Integer i1, String s1) {
        this.i1 = i1;
        this.s1 = s1;
    }
}