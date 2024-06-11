package collection;

import com.bawnorton.autocodec.AutoCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

@AutoCodec
public class SingleList {
    public static final Codec<SingleList> EXISTING_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("list").forGetter(s -> s.list)
    ).apply(instance, SingleList::new));

    private SingleList(List<String> list) {
        this.list = list;
    }

    private final List<String> list;
}