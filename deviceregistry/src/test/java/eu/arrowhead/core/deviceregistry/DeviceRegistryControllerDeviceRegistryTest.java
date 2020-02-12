package eu.arrowhead.core.deviceregistry;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.core.deviceregistry.database.service.DeviceRegistryDBService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {DeviceRegistryTestContext.class})
public class DeviceRegistryControllerDeviceRegistryTest {


    //=================================================================================================
    // members

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "mockDeviceRegistryDBService")
    private DeviceRegistryDBService deviceRegistryDBService;

    @MockBean(name = "mockCommonDBService")
    private CommonDBService commonDBService;


    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void getDeviceRegistryEntries() {
    }

    @Test
    public void getDeviceRegistryEntryById() {
    }

    @Test
    public void getDeviceRegistryEntriesByDeviceName() {
    }

    @Test
    public void removeDeviceRegistryEntryById() {
    }

    @Test
    public void updateDeviceRegistry() {
    }

    @Test
    public void mergeDeviceRegistry() {
    }

    @Test
    public void queryRegistry() {
    }

    @Test
    public void queryRegistryByDeviceId() {
    }

    @Test
    public void queryRegistryByDeviceDTO() {
    }

    //=================================================================================================
    // Tests of device registry

}