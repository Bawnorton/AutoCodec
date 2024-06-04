import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.Optional;

@AutoCodec
public record SimpleRecord(String included, @Optional String optional, @Ignore Integer notIncluded) {
}