import com.bawnorton.autocodec.AutoCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@AutoCodec
public record AlternateCodec(Integer integer, String string) {
    public static final Codec<AlternateCodec> EXISTING_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("integer").forGetter(ac -> ac.integer),
            Codec.STRING.fieldOf("string").forGetter(ac -> ac.string)
    ).apply(instance, AlternateCodec::new));
}
