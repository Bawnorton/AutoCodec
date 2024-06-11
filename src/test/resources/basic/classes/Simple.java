package basic.classes;

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.OptionalEntry;

@AutoCodec
public class Simple {
    private String included;
    @OptionalEntry
    private String optional;
    @Ignore
    private Integer notIncluded;
}