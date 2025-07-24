package net.furizon.jooq.strategies;

import net.furizon.jooq.DeserializableJooqEnum;
import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.Definition;
import org.jooq.meta.EnumDefinition;

import java.util.List;

public class FzGeneratorStrategy extends DefaultGeneratorStrategy {

    @Override
    public List<String> getJavaClassImplements(Definition definition, Mode mode) {
        List<String> toReturn = super.getJavaClassImplements(definition, mode);
        if (definition instanceof  EnumDefinition) {
            toReturn.add(DeserializableJooqEnum.class.getCanonicalName());
        }
        return toReturn;
    }
}
