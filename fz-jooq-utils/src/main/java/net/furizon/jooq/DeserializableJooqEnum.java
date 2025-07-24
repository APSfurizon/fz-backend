package net.furizon.jooq;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.furizon.jooq.strategies.utils.EnumTypeDeserializer;
import org.jooq.EnumType;

@JsonDeserialize(using = EnumTypeDeserializer.class)
public interface DeserializableJooqEnum extends EnumType {
}
