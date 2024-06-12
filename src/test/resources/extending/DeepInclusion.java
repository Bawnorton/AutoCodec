package extending;

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.IncludeInChildren;

@AutoCodec
public class DeepInclusion extends DeepInclusionParent {
}

class DeepInclusionParent extends DeepInclusionGrandParent {
}

class DeepInclusionGrandParent {
    @IncludeInChildren
    protected String string;
}
