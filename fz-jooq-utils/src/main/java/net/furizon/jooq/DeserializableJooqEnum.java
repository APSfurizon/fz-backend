package net.furizon.jooq;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import net.furizon.jooq.strategies.utils.EnumTypeDeserializer;
import net.furizon.jooq.strategies.utils.EnumTypeSerializer;
import org.jooq.EnumType;

@JsonDeserialize(using = EnumTypeDeserializer.class)
@JsonSerialize(using = EnumTypeSerializer.class)
public interface DeserializableJooqEnum extends EnumType {
}
