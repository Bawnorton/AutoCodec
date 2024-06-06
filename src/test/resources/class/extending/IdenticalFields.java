import com.bawnorton.autocodec.AutoCodec;

@AutoCodec
public class IdenticalFields extends Parent {
    private String string;
    private Integer integer;
}

class Parent {
    private String string;
    private Integer integer;

    public Parent(String string, Integer integer) {
        this.string = string;
        this.integer = integer;
    }
}