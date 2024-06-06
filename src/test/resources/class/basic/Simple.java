import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.Optional;

@AutoCodec
public class Simple {
    private String included;
    @Optional
    private String optional;
    @Ignore
    private Integer notIncluded;
}