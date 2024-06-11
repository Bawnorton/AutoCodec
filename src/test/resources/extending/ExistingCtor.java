package extending;

import com.bawnorton.autocodec.AutoCodec;

@AutoCodec
public class ExistingCtor extends ExistingCtorParent {
    private String s1;
    private String s2;

    public ExistingCtor(String s1, String s2) {
        super(null, s1);
        this.s2 = s2;
    }
}

class ExistingCtorParent {
    private Integer i1;
    private String s1;

    public ExistingCtorParent(Integer i1, String s1) {
        this.i1 = i1;
        this.s1 = s1;
    }
}