package extending;

import com.bawnorton.autocodec.AutoCodec;

@AutoCodec
public class DifferentFields extends DifferentFieldsParent {
    private String s1;
    private String s2;

}

class DifferentFieldsParent {
    private Integer i1;
}