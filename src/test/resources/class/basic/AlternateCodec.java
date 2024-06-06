import com.bawnorton.autocodec.AutoCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@AutoCodec
public class AlternateCodec {
    public static final Codec<AlternateCodecClass> EXISTING_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.<Integer>fieldOf("integer").<AlternateCodecClass>forGetter(alternateCodec -> alternateCodec.integer),
            Codec.STRING.<String>fieldOf("string").<AlternateCodecClass>forGetter(alternateCodec -> alternateCodec.string)
    ).<AlternateCodecClass>apply(instance, AlternateCodecClass::new));

    private final Integer integer;
    private final String string;

    public AlternateCodec(Integer integer, String string) {
        this.integer = integer;
        this.string = string;
    }
}
