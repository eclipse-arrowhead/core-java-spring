package eu.arrowhead.core.gams;

public class Constants {

    public static final String PLACEHOLDER_START = "${";
    public static final String PLACEHOLDER_END = "}";

    public static final String PATH_ROOT = "/";

    public static final String PARAMETER_UID = "uid";
    public static final String PATH_UID = "/" + PARAMETER_UID;
    public static final String PATH_PARAMETER_UID = "/{" + PARAMETER_UID + "}";

    public static final String PARAMETER_SENSOR = "sensor";
    public static final String PATH_SENSOR = "/" + PARAMETER_SENSOR;
    public static final String PATH_PARAMETER_SENSOR = "/{" + PARAMETER_SENSOR + "}";

    public static final String PARAMETER_NAME = "name";
    public static final String PATH_NAME = "/" + PARAMETER_NAME;
    public static final String PATH_PARAMETER_NAME = "/{" + PARAMETER_NAME + "}";

    public static final String PARAMETER_ADDRESS = "address";
    public static final String PATH_ADDRESS = "/" + PARAMETER_ADDRESS;
    public static final String PATH_PARAMETER_ADDRESS = "/{" + PARAMETER_ADDRESS + "}";

}
