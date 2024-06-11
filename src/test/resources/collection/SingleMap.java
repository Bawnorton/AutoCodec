package collection;

import com.bawnorton.autocodec.AutoCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;

@AutoCodec
public class SingleMap {
    public static final Codec<SingleMap> EXISTING_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("list").forGetter(s -> s.map)
    ).apply(instance, SingleMap::new));

    private SingleMap(Map<String, String> map) {
        this.map = map;
    }

    private final Map<String, String> map;
}