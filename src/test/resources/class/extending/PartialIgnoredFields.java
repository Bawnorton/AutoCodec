import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.Ignore;

@AutoCodec
public class PartialIgnoredFields extends Parent {
    private String s1;
    private String s2;
    private Integer i1;
    @Ignore
    private Integer i2;

    public PartialIgnoredFields(Integer i1, String s1, String s2, Integer i2) {
        super(i1, s1);
        this.s1 = s1;
        this.s2 = s2;
        this.i1 = i1;
        this.i2 = i2;
    }
}

class Parent {
    private Integer i2;
    private String s1;

    public Parent(Integer i2, String s1) {
        this.i2 = i2;
        this.s1 = s1;
    }
}