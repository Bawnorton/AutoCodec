import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.OptionalEntry;
import java.util.List;

@AutoCodec
public class ExpressionResolver {
    @OptionalEntry("List.of(\"defaultValue\").get(0)")
    private String optional;
}