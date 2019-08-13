package eu.arrowhead.core.choreographer;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.arrowhead.core.choreographer.database.service.ChoreographerDBService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChoreographerMain.class)
@ContextConfiguration(classes = { ChoreographerDBServiceTestContext.class })
public class ChoreographerControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ChoreographerController controller;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "mockChoreographerDBService")
    ChoreographerDBService choreographerDBService;

    private static final String SYSTEMS_URL = "/choreographer/mgmt/actionplan/";
    private static final String PAGE = "page";
    private static final String ITEM_PER_PAGE = "item_per_page";

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }



}
