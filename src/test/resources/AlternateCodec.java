import com.bawnorton.autocodec.AutoCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@AutoCodec
public record AlternateCodec(Integer integer, String string) {
    public static final Codec<AlternateCodec> EXISTING_CODEC = RecordCodecBuilder.<AlternateCodec>create((RecordCodecBuilder.Instance<AlternateCodec> instance) -> instance.<Integer, String>group(
            Codec.INT.<Integer>fieldOf("integer").<AlternateCodec>forGetter(AlternateCodec::integer),
            Codec.STRING.<String>fieldOf("string").<AlternateCodec>forGetter(AlternateCodec::string)
    ).<AlternateCodec>apply(instance, AlternateCodec::new));
}
