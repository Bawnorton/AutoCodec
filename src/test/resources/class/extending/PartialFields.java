import com.bawnorton.autocodec.AutoCodec;

@AutoCodec
public class PartialFields extends Parent {
    private String s1;
    private String s2;
}

class Parent {
    private Integer i1;
    private String s1;

    public Parent(Integer i1, String s1) {
        this.i1 = i1;
        this.s1 = s1;
    }
}