import com.bawnorton.autocodec.AutoCodec;

@AutoCodec
public class ExistingCtorDifOrder {
    private String s1;
    private String s2;
    private Integer i1;

    public ExistingCtorDifOrder(Integer i1, String s2, String s1) {
        this.s1 = s1;
        this.s2 = s2;
        this.i1 = i1;
    }
}