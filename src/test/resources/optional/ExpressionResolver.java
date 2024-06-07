import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.OptionalEntry;
import java.util.List;

@AutoCodec
public class ExpressionResolver {
    @OptionalEntry("List.of(\"defaultValue\").get(0)")
    private String optional;
}

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.OptionalEntry;
import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

@AutoCodec
public class ExpressionResolver {
    public static final Codec<ExpressionResolver> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.optionalFieldOf("optional").xmap(
                            optionalOptional -> optionalOptional.orElse(List.of("defaultValue").get(0)),
                            Optional::ofNullable
                    ).forGetter(ExpressionResolver::optional)
            ).apply(instance, ExpressionResolver::new));

    public ExpressionResolver() {
        super();
    }
    @OptionalEntry(value = "List.of(\"defaultValue\").get(0)")
    private String optional;

    public ExpressionResolver(String optional) {
        super();
        this.optional = optional;
    }

    private String optional() {
        return optional;
    }
}