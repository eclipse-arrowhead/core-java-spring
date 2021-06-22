package eu.arrowhead.core.translator;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import javax.annotation.Resource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import eu.arrowhead.common.CoreCommonConstants;
import org.junit.Before;
import org.junit.Test;

@RunWith(SpringRunner.class)
public class TranslatorControllerTest {

    //=================================================================================================
    // members

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @Before
    public void setup() {
        System.out.println("\n\n\t SETUP \n\n\n");
    }

    //-------------------------------------------------------------------------------------------------
    @Test
    public void testCase1() throws Exception {
        System.out.println("\n\n\t Test Case 1 \n\n\n");
    }

    //=================================================================================================
    // assistant methods
    //-------------------------------------------------------------------------------------------------
}
