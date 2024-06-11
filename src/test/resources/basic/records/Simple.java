package basic.records;

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.OptionalEntry;

@AutoCodec
public record Simple(String included, @OptionalEntry String optional, @Ignore Integer notIncluded) {
}