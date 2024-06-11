package extending;

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.Ignore;

@AutoCodec
public class PartialIgnoredFields extends PartialIgnoredFieldsParent {
    private String s1;
    private String s2;
    private Integer i1;
    @Ignore
    private Integer i2;

    public PartialIgnoredFields(Integer i2, String s1) {
        super(i2, s1);
    }
}

class PartialIgnoredFieldsParent {
    private Integer i2;
    private String s1;

    public PartialIgnoredFieldsParent(Integer i2, String s1) {
        this.i2 = i2;
        this.s1 = s1;
    }
}