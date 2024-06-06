import com.bawnorton.autocodec.AutoCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@AutoCodec
public record AlternateCodec(Integer integer, String string) {
    public static final Codec<AlternateCodecRecord> EXISTING_CODEC = RecordCodecBuilder.<AlternateCodecRecord>create((RecordCodecBuilder.Instance<AlternateCodecRecord> instance) -> instance.<Integer, String>group(
            Codec.INT.<Integer>fieldOf("integer").<AlternateCodecRecord>forGetter(ac -> ac.integer),
            Codec.STRING.<String>fieldOf("string").<AlternateCodecRecord>forGetter(ac -> ac.string)
    ).<AlternateCodecRecord>apply(instance, AlternateCodecRecord::new));
}
