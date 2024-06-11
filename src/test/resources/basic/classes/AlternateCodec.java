package basic.classes;

import com.bawnorton.autocodec.AutoCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@AutoCodec
public class AlternateCodec {
    public static final Codec<AlternateCodec> EXISTING_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("integer").forGetter(alternateCodec -> alternateCodec.integer),
            Codec.STRING.fieldOf("string").forGetter(alternateCodec -> alternateCodec.string)
    ).apply(instance, AlternateCodec::new));

    private final Integer integer;
    private final String string;

    public AlternateCodec(Integer integer, String string) {
        this.integer = integer;
        this.string = string;
    }
}
