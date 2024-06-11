package basic.classes;

import com.bawnorton.autocodec.AutoCodec;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@AutoCodec
public class AllTypes {
    private String string;
    private Integer integer;
    private Long long_;
    private Float float_;
    private Double double_;
    private Boolean bool;
    private Byte byte_;
    private Short short_;
    private ByteBuffer byteBuffer;
    private IntStream intStream;
    private LongStream longStream;
}