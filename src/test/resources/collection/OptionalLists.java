package collection;

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.OptionalEntry;
import java.util.List;
import java.util.ArrayList;

@AutoCodec
public class OptionalLists {
    @OptionalEntry
    private List<String> list;
    @OptionalEntry("new ArrayList<>()")
    private List<Integer> intList;
    @OptionalEntry("defaultBoolList")
    private List<Boolean> booleanList;

    private static List<Boolean> defaultBoolList() {
        return List.of(true, false);
    }
}
