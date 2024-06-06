import com.bawnorton.autocodec.AutoCodec;

@AutoCodec
public class DifferentFields extends Parent {
    private String s1;
    private String s2;
}

class Parent {
    private Integer i1;

    public Parent(Integer i1) {
        this.i1 = i1;
    }
}