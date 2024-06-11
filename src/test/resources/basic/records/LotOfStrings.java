package basic.records;

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.Ignore;

@AutoCodec
public record LotOfStrings(String s1, String s2, @Ignore String s3, String s4, String s5, @Ignore String s6, @Ignore String s7, String s8) {
}