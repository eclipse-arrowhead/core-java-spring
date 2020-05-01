package eu.arrowhead.core.mscv;

import eu.arrowhead.common.CoreCommonConstants;

public class Constants {


    public static final String PARAMETER_NAME = "name";
    public static final String PARAMETER_NAME_PATH = "/{" + PARAMETER_NAME + "}";

    public static final String PARAMETER_IDENTIFICATION = "identification";
    public static final String PARAMETER_IDENTIFICATION_PATH = "/{" + PARAMETER_IDENTIFICATION + "}";

    public static final String PARAMETER_ADDRESS = "address";
    public static final String PATH_ADDRESS = "/" + PARAMETER_ADDRESS;
    public static final String PARAMETER_ADDRESS_PATH = "/{" + PARAMETER_ADDRESS + "}";

    public static final String PARAMETER_PORT = "port";
    public static final String PATH_PORT = "/" + PARAMETER_PORT;
    public static final String PARAMETER_PORT_PATH = "/{" + PARAMETER_PORT + "}";

    public static final String PARAMETER_MIP_ID = "mipId";
    public static final String PATH_MIP_ID = "/" + PARAMETER_MIP_ID;
    public static final String PARAMETER_MIP_ID_PATH = "/{" + PARAMETER_MIP_ID + "}";

    public static final String PARAMETER_OS = "os";
    public static final String PATH_OS = "/" + PARAMETER_OS;
    public static final String PARAMETER_OS_PATH = "/{" + PARAMETER_OS + "}";

    public static final String PARAMETER_LAYER = "layer";
    public static final String PATH_LAYER = "/" + PARAMETER_LAYER;
    public static final String PARAMETER_LAYER_PATH = "/{" + PARAMETER_LAYER + "}";

    public static final String SWAGGER_TAG_CATEGORY = CoreCommonConstants.SWAGGER_TAG_MGMT + " of Categories";
    public static final String SWAGGER_TAG_DOMAIN = CoreCommonConstants.SWAGGER_TAG_MGMT + " of Domains";
    public static final String SWAGGER_TAG_MIP = CoreCommonConstants.SWAGGER_TAG_MGMT + " of Measurable Indicator Points";
    public static final String SWAGGER_TAG_SCRIPT = CoreCommonConstants.SWAGGER_TAG_MGMT + " of Scripts";
    public static final String SWAGGER_TAG_STANDARD = CoreCommonConstants.SWAGGER_TAG_MGMT + " of Standards";
    public static final String SWAGGER_TAG_TARGET = CoreCommonConstants.SWAGGER_TAG_MGMT + " of Targets";

    private Constants() { throw new IllegalAccessError(); }
}