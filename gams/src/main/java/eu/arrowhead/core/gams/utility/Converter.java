package eu.arrowhead.core.gams.utility;

import java.util.Objects;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.core.gams.rest.dto.GamsInstanceDto;
import eu.arrowhead.core.gams.rest.dto.SensorDto;

public class Converter {

    public static GamsInstanceDto convert(final GamsInstance instance) {
        if (Objects.isNull(instance)) { return null; } else {
            return new GamsInstanceDto(instance.getName(),
                                       instance.getUidAsString(),
                                       Utilities.convertZonedDateTimeToUTCString(instance.getCreatedAt()),
                                       instance.getDelay(),
                                       instance.getOwner());
        }
    }

    public static SensorDto convert(final Sensor sensor) {
        if (Objects.isNull(sensor)) { return null; } else {
            return new SensorDto(sensor.getUidAsString(),
                                 sensor.getAddress(),
                                 sensor.getName(),
                                 sensor.getType(),
                                 sensor.getRetentionTime(),
                                 sensor.getTimeUnit());
        }
    }
}
