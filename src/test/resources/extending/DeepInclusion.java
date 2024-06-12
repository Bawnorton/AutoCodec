package extending;

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.IncludeInChildren;

@AutoCodec
public class DeepInclusion extends DeepInclusionParent {
    private String string2;

    public DeepInclusion(String string) {
        super(string);
    }
}

class DeepInclusionParent extends DeepInclusionGrandParent {
    public DeepInclusionParent(String string) {
        super(string);
    }
}

class DeepInclusionGrandParent {
    @IncludeInChildren
    protected String string;

    public DeepInclusionGrandParent(String string) {
        this.string = string;
    }
}