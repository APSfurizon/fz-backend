package net.furizon.jooq.strategies.utils;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.deser.std.StdDeserializer;
import org.jooq.EnumType;

public class EnumTypeDeserializer<E extends Enum<E> & EnumType> extends StdDeserializer<E> {

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
    public E deserialize(JsonParser jsonParser,
                         DeserializationContext deserializationContext
    ) throws JacksonException {
        String value = jsonParser.getString();
        return enumType == null ? null : EnumType.lookupLiteral(enumType, value);
    }

    @Override
    public ValueDeserializer<?> createContextual(
            DeserializationContext deserializationContext, BeanProperty beanProperty) throws DatabindException {
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
