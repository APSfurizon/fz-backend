package net.furizon.jooq.strategies.utils;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import org.jooq.EnumType;

public class EnumTypeSerializer<E extends Enum<E> & EnumType> extends StdSerializer<E> {

    protected EnumTypeSerializer() {
        super((Class<E>) null);
    }

    protected EnumTypeSerializer(Class<E> t) {
        super(t);
    }

    @Override
    public void serialize(E e, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
        jsonGenerator.writeString(e.getLiteral());
    }
}
