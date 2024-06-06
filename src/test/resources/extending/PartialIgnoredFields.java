import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.Ignore;

@AutoCodec
public class PartialIgnoredFields extends Parent {
    private String s1;
    private String s2;
    private Integer i1;
    @Ignore
    private Integer i2;
}

class Parent {
    private Integer i2;
    private String s1;

    public Parent(Integer i2, String s1) {
        this.i2 = i2;
        this.s1 = s1;
    }
}