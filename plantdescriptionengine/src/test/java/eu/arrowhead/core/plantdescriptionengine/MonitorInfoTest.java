package eu.arrowhead.core.plantdescriptionengine;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertFalse;


public class MonitorInfoTest {

    @Test
    public void shouldNotMatchWhenMetadataIsEmpty() {

        final String systemName = "systemx";
        final String serviceDefinition = "service-a";

        MonitorInfo info = new MonitorInfo(
            systemName,
            serviceDefinition,
            Collections.emptyMap(),
            Collections.emptyMap(),
            null,
            null
        );

        Map<String, String> metadata = Map.of("a", "1");

        assertFalse(info.matches(systemName, metadata));
    }

}
