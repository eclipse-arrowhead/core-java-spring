package eu.arrowhead.common.database;

import java.util.Set;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

// https://stackoverflow.com/questions/287201/how-to-persist-a-property-of-type-liststring-in-jpa
@Converter
public class StringSetConverter implements AttributeConverter<Set<String>, String> {
    private static final String SPLIT_CHAR = ";";

    @Override
    public String convertToDatabaseColumn(final Set<String> strings) {
        return strings != null ? String.join(SPLIT_CHAR, strings) : "";
    }

    @Override
    public Set<String> convertToEntityAttribute(final String string) {
        return string != null ? Set.of(string.split(SPLIT_CHAR)) : Set.of();
    }
}
