import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.OptionalEntry;

@AutoCodec
public class MethodResolver {
    @OptionalEntry("defaultResolver")
    private String optional;

    private static String defaultResolver() {
        return "defaultValue";
    }
}