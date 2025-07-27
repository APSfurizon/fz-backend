package net.furizon.jooq.strategies.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.jooq.EnumType;

import java.io.IOException;

public class EnumTypeDeserializer<E extends Enum<E> & EnumType> extends StdDeserializer<E>
        implements ContextualDeserializer {

    private Class<E> enumType;

    protected EnumTypeDeserializer() {
        super((Class<E>) null);
        this.enumType = null;
    }

    protected EnumTypeDeserializer(Class<?> vc) {
        super(vc);
        enumType = (Class<E>) vc;
    }

    @Override
    public E deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JacksonException {
        String value = jsonParser.getText();
        return enumType == null ? null : EnumType.lookupLiteral(enumType, value);
    }

    @Override
    public JsonDeserializer<?> createContextual(
            DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        JavaType type = deserializationContext.getContextualType();
        if (type == null && beanProperty != null) {
            type = beanProperty.getType();
        }
        if (type == null) {
            return this;  // fallback
        }

        Class<?> raw = type.getRawClass();

        return new EnumTypeDeserializer<E>(raw);
    }
}
