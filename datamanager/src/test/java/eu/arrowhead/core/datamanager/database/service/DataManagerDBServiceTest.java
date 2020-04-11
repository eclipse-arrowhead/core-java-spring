package eu.arrowhead.core.datamanager.database.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.datamanager.service.DataManagerDriver;
import eu.arrowhead.core.datamanager.service.DataManagerService;

@RunWith(SpringRunner.class)
public class DataManagerDBServiceTest {

    //=================================================================================================
    // members

    //@InjectMocks
    //private DataManagerDBService datamanagerDBService = new DataManagerDBService();

    @Mock
    private DataManagerService dataManagerService;
	
    @Mock
    private DataManagerDriver dataManagerDriver;

    //=================================================================================================
    // methods
    
    @Test
    public void getServicesFromFakeSystem() {
	/*final ArrayList<String> response = datamanagerDBService.getServicesFromSystem("nonExistingFakeTestOnlySystem");
	
	assertNotNull(response);
	assertTrue(response.size() == 0);*/
    }
}

