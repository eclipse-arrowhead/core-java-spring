package eu.arrowhead.core.choreographer.service;

import com.rabbitmq.client.Command;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ChoreographerDriverTest {

    //=================================================================================================
    //  members

    @InjectMocks
    private ChoreographerDriver choreographerDriver;

    @Mock
    private HttpService httpService;

    @Mock
    private Map<String, Object> arrowheadContext;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Test
    public void testQueryServiceRegistryByServiceDefinitionListOk() {
        final UriComponents queryByServiceDefinitionListUri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8443, CommonConstants.SERVICEREGISTRY_URI +
                CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_SERVICES_BY_SERVICE_DEFINITION_LIST_URI);

        final ServiceRegistryListResponseDTO responseDTO = new ServiceRegistryListResponseDTO();
        final List<String> request = new ArrayList<>();
        request.add("Test");

        when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
        when(arrowheadContext.get(any(String.class))).thenReturn(queryByServiceDefinitionListUri);
        when(httpService.sendRequest(eq(queryByServiceDefinitionListUri), eq(HttpMethod.POST), eq(ServiceRegistryListResponseDTO.class), anyList())).thenReturn(new ResponseEntity<ServiceRegistryListResponseDTO>(responseDTO, HttpStatus.OK));

        final ServiceRegistryListResponseDTO serviceRegistryListResponseDTO = choreographerDriver.queryServiceRegistryByServiceDefinitionList(request);

        Assert.assertNotNull(serviceRegistryListResponseDTO);
    }

    @Test(expected = ArrowheadException.class)
    public void testQueryServiceRegistryByServiceDefinitionListApplicationContextReturningIncorrectClass() {
        final List<String> requestList = new ArrayList<>();

        when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
        when(arrowheadContext.get(any(String.class))).thenReturn(new Object());

        choreographerDriver.queryServiceRegistryByServiceDefinitionList(requestList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryServiceRegistryByServiceDefinitionListNullList() {
        choreographerDriver.queryServiceRegistryByServiceDefinitionList(null);
    }


}
