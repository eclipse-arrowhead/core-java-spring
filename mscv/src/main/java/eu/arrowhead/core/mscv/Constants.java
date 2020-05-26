package eu.arrowhead.core.mscv;

import eu.arrowhead.common.CoreCommonConstants;

public class Constants {


    public static final String PARAMETER_NAME = "name";
    public static final String PATH_NAME = "/" + PARAMETER_NAME;
    public static final String PARAMETER_NAME_PATH = "/{" + PARAMETER_NAME + "}";

    public static final String PARAMETER_TARGET_NAME = "target";
    public static final String PATH_TARGET_NAME = "/" + PARAMETER_TARGET_NAME;
    public static final String PARAMETER_TARGET_NAME_PATH = "/{" + PARAMETER_TARGET_NAME + "}";

    public static final String PARAMETER_IDENTIFICATION = "identification";
    public static final String PARAMETER_IDENTIFICATION_PATH = "/{" + PARAMETER_IDENTIFICATION + "}";

    public static final String PARAMETER_ADDRESS = "address";
    public static final String PATH_ADDRESS = "/" + PARAMETER_ADDRESS;
    public static final String PARAMETER_ADDRESS_PATH = "/{" + PARAMETER_ADDRESS + "}";

    public static final String PARAMETER_PORT = "port";
    public static final String PATH_PORT = "/" + PARAMETER_PORT;
    public static final String PARAMETER_PORT_PATH = "/{" + PARAMETER_PORT + "}";

    public static final String PARAMETER_MIP_EXT_ID = "mipId";
    public static final String PATH_MIP_EXT_ID = "/" + PARAMETER_MIP_EXT_ID;
    public static final String PARAMETER_MIP_EXT_ID_PATH = "/{" + PARAMETER_MIP_EXT_ID + "}";

    public static final String PARAMETER_MIP_IDENTIFIER = "mip";
    public static final String PATH_MIP_IDENTIFIER = "/" + PARAMETER_MIP_IDENTIFIER;
    public static final String PARAMETER_MIP_IDENTIFIER_PATH = "/{" + PARAMETER_MIP_IDENTIFIER + "}";

    public static final String PARAMETER_MIP_CATEGORY = "category";
    public static final String PATH_MIP_CATEGORY = "/" + PARAMETER_MIP_CATEGORY;
    public static final String PARAMETER_MIP_CATEGORY_PATH = "/{" + PARAMETER_MIP_CATEGORY + "}";

    public static final String PARAMETER_MIP_CATEGORY_ABBREVIATION = "abbreviation";
    public static final String PATH_MIP_CATEGORY_ABBREVIATION = "/" + PARAMETER_MIP_CATEGORY_ABBREVIATION;
    public static final String PARAMETER_MIP_CATEGORY_ABBREVIATION_PATH = "/{" + PARAMETER_MIP_CATEGORY_ABBREVIATION + "}";

    public static final String PARAMETER_MIP_ABBREVIATION = "abbreviation";
    public static final String PATH_MIP_ABBREVIATION = "/" + PARAMETER_MIP_ABBREVIATION;
    public static final String PARAMETER_MIP_ABBREVIATION_PATH = "/{" + PARAMETER_MIP_ABBREVIATION + "}";

    public static final String PARAMETER_MIP_STANDARD = "standard";
    public static final String PATH_MIP_STANDARD = "/" + PARAMETER_MIP_STANDARD;
    public static final String PARAMETER_MIP_STANDARD_PATH = "/{" + PARAMETER_MIP_STANDARD + "}";

    public static final String PARAMETER_MIP_DOMAIN = "domain";
    public static final String PATH_MIP_DOMAIN = "/" + PARAMETER_MIP_DOMAIN;
    public static final String PARAMETER_MIP_DOMAIN_PATH = "/{" + PARAMETER_MIP_DOMAIN + "}";

    public static final String PARAMETER_OS = "os";
    public static final String PATH_OS = "/" + PARAMETER_OS;
    public static final String PARAMETER_OS_PATH = "/{" + PARAMETER_OS + "}";

    public static final String PARAMETER_LAYER = "layer";
    public static final String PATH_LAYER = "/" + PARAMETER_LAYER;
    public static final String PARAMETER_LAYER_PATH = "/{" + PARAMETER_LAYER + "}";

    public static final String SWAGGER_TAG_CATEGORY_MGMT = CoreCommonConstants.SWAGGER_TAG_MGMT + " of Categories";
    public static final String SWAGGER_TAG_DOMAIN_MGMT = CoreCommonConstants.SWAGGER_TAG_MGMT + " of Domains";
    public static final String SWAGGER_TAG_MIP_MGMT = CoreCommonConstants.SWAGGER_TAG_MGMT + " of Measurable Indicator Points";
    public static final String SWAGGER_TAG_SCRIPT_MGMT = CoreCommonConstants.SWAGGER_TAG_MGMT + " of Scripts";
    public static final String SWAGGER_TAG_STANDARD_MGMT = CoreCommonConstants.SWAGGER_TAG_MGMT + " of Standards";
    public static final String SWAGGER_TAG_TARGET_MGMT = CoreCommonConstants.SWAGGER_TAG_MGMT + " of Targets";
    public static final String SWAGGER_TAG_VERIFICATION_MGMT = CoreCommonConstants.SWAGGER_TAG_MGMT + " of Results";

    private Constants() { throw new IllegalAccessError(); }
}
