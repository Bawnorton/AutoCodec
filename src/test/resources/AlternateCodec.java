import com.bawnorton.autocodec.AutoCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@AutoCodec
public record AlternateCodec(String string, Integer integer) {
    public static final Codec<AlternateCodec> EXISTING_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("string").forGetter(AlternateCodec::string),
        Codec.INT.fieldOf("integer").forGetter(AlternateCodec::integer)
    ).apply(instance, AlternateCodec::new));
}