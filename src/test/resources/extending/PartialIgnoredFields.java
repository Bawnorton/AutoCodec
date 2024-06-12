package extending;

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.IncludeInChildren;

@AutoCodec
public class PartialIgnoredFields extends PartialIgnoredFieldsParent {
    private String s1;
    private String s2;
    private int i1;
    @Ignore
    private int i2;

    public PartialIgnoredFields(int i2, String s1) {
        super(i2, s1);
    }
}

class PartialIgnoredFieldsParent {
    @IncludeInChildren
    protected int i2;
    private String s1;

    public PartialIgnoredFieldsParent(int i2, String s1) {
        this.i2 = i2;
        this.s1 = s1;
    }
}