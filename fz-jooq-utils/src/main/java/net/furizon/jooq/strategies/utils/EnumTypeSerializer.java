package net.furizon.jooq.strategies.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jooq.EnumType;

import java.io.IOException;

public class EnumTypeSerializer<E extends Enum<E> & EnumType> extends StdSerializer<E> {

    protected EnumTypeSerializer() {
        super((Class<E>) null);
    }

    protected EnumTypeSerializer(Class<E> t) {
        super(t);
    }

    @Override
    public void serialize(E e, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(e.getLiteral());
    }
}
