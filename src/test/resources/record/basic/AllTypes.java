import com.bawnorton.autocodec.AutoCodec;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@AutoCodec
public record AllTypes(
        String string,
        Integer integer,
        Long long_,
        Float float_,
        Double double_,
        Boolean bool,
        Byte byte_,
        Short short_,
        ByteBuffer byteBuffer,
        IntStream intStream,
        LongStream longStream) {

}