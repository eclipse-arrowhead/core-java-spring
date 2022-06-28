package eu.arrowhead.common.database;

import java.util.Map;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import eu.arrowhead.common.Utilities;

// https://stackoverflow.com/questions/287201/how-to-persist-a-property-of-type-liststring-in-jpa
@Converter
public class StringMapConverter implements AttributeConverter<Map<String, String>, String> {

    @Override
    public String convertToDatabaseColumn(final Map<String, String> strings) {
        return Utilities.map2Text(strings);
    }

    @Override
    public Map<String, String> convertToEntityAttribute(final String string) {
        return Utilities.text2Map(string);
    }
}
