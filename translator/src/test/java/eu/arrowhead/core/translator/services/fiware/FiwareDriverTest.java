package eu.arrowhead.core.translator.services.fiware;

import org.junit.Test;

public class FiwareDriverTest {
    //=================================================================================================
    // members

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @Test(expected = RuntimeException.class)
    public void constructorNullTest() {
        FiwareDriver fiwareDriver = new FiwareDriver(null, null, 0);
    }
}
