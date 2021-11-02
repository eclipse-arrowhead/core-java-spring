package eu.arrowhead.core.serviceregistry.service;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CoreCommonConstants;

@RunWith (SpringRunner.class)
public class ServiceRegistryServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private ServiceRegistryService testingObject;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	
	@Test
	public void getPublicConfigTest() {
		ReflectionTestUtils.setField(testingObject, "useStrictServiceDefinitionVerifier", true);
		ReflectionTestUtils.setField(testingObject, "allowSelfAddressing", false);
		ReflectionTestUtils.setField(testingObject, "allowNonRoutableAddressing", false);
		
		final Map<String, String> resultMap = testingObject.getPublicConfig().getMap();
		
		Assert.assertTrue(Boolean.valueOf(resultMap.get(CoreCommonConstants.USE_STRICT_SERVICE_DEFINITION_VERIFIER)));
		Assert.assertFalse(Boolean.valueOf(resultMap.get(CoreCommonConstants.ALLOW_SELF_ADDRESSING)));
		Assert.assertFalse(Boolean.valueOf(resultMap.get(CoreCommonConstants.ALLOW_NON_ROUTABLE_ADDRESSING)));
	}
}
