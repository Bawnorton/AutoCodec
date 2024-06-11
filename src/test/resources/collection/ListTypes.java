package collection;

import com.bawnorton.autocodec.AutoCodec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.List;

@AutoCodec
public class ListTypes {
    private ArrayList<String> list;
    private IntArrayList intList;
    private List<Boolean> booleanList;
}