package eu.arrowhead.core.deviceregistry;

class Constants {

    //=================================================================================================
    // members
    static final String PATH_VARIABLE_ID = "deviceId";
    static final String PATH_VARIABLE_DEVICE_NAME = "deviceName";

    static final String DEVICES_URI = "/devices";
    static final String DEVICE_BY_ID_URI = "/device/{" + PATH_VARIABLE_ID + "}";
    static final String DEVICES_BY_ID_URI = "/devices/{" + PATH_VARIABLE_ID + "}";

    protected Constants() { super(); }
}
