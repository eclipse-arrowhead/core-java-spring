package eu.arrowhead.core.gatekeeper.database.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeper;
import eu.arrowhead.common.database.repository.CloudGatekeeperRepository;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith (SpringRunner.class)
public class GatekeeperDBServiceTest {


	//=================================================================================================
	// members
	
	@InjectMocks
	GatekeeperDBService gatekeeperDBService;
	
	@Mock
	CloudGatekeeperRepository cloudGatekeeperRepository;
	
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	//Tests of getGatekeeperByCloud
	
	@Test
	public void testGetGatekeeperByCloudValidCloud() {
		final Cloud cloud = new Cloud();
		cloud.setId(1);
		final CloudGatekeeper cloudGatekeeper = new CloudGatekeeper(cloud, "", 1000, "", null);
		when(cloudGatekeeperRepository.findByCloud(any())).thenReturn(Optional.of(cloudGatekeeper));
		
		final CloudGatekeeper gatekeeperByCloud = gatekeeperDBService.getGatekeeperByCloud(cloud);
		assertEquals(1, gatekeeperByCloud.getCloud().getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetGatekeeperByCloudWithInvalidCloud() {
		when(cloudGatekeeperRepository.findByCloud(any())).thenReturn(Optional.ofNullable(null));
		gatekeeperDBService.getGatekeeperByCloud(new Cloud());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of registerGatekeeper
	
	@Test(expected = InvalidParameterException.class)
	public void testRegisterGatekeeperWithNullCloud() {		
		gatekeeperDBService.registerGatekeeper(null, "1.1.1.1", 1000, "/testuri", "fewrdc");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterGatekeeperWithNullAddress() {		
		gatekeeperDBService.registerGatekeeper(new Cloud(), null, 1000, "/testuri", "fewrdc");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterGatekeeperWithBlankAddress() {		
		gatekeeperDBService.registerGatekeeper(new Cloud(), "", 1000, "/testuri", "fewrdc");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterGatekeeperWithInvlaidMinPort() {		
		gatekeeperDBService.registerGatekeeper(new Cloud(), "1.1.1.1", CommonConstants.SYSTEM_PORT_RANGE_MIN - 1, "/testuri", "fewrdc");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterGatekeeperWithInvlaidMaxPort() {		
		gatekeeperDBService.registerGatekeeper(new Cloud(), "1.1.1.1", CommonConstants.SYSTEM_PORT_RANGE_MAX + 1, "/testuri", "fewrdc");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterGatekeeperWithNullUri() {		
		gatekeeperDBService.registerGatekeeper(new Cloud(), "1.1.1.1", 1000, null, "fewrdc");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterGatekeeperWithBlankUri() {		
		gatekeeperDBService.registerGatekeeper(new Cloud(), "1.1.1.1", 1000, "", "fewrdc");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterGatekeeperWithSecureCloudButhWithoutAuthInfo() {	
		final Cloud cloud = new Cloud();
		cloud.setSecure(true);
		gatekeeperDBService.registerGatekeeper(cloud, "2.2.2.2", 2000, "/testUri", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterGatekeeperWithSecureCloudButhWithBlankAuthInfo() {	
		final Cloud cloud = new Cloud();
		cloud.setSecure(true);
		gatekeeperDBService.registerGatekeeper(cloud, "2.2.2.2", 2000, "/testUri", "");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterGatekeeperWithCloudAlreadyHavingGatekeeper() {
		final Cloud cloud = new Cloud();
		final CloudGatekeeper cloudGatekeeper = new CloudGatekeeper(cloud, "0.0.0.0", 1000, "", null);
		when(cloudGatekeeperRepository.findByCloud(any())).thenReturn(Optional.of(cloudGatekeeper));
		
		gatekeeperDBService.registerGatekeeper(cloud, "1.1.1.1", 2000, "", "fewrdc");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterGatekeeperWithAlreadyExistingAdrressPortUriCombination() {
		final String address = "1.1.1.1";
		final int port = 1000;
		final String uri = "/testuri";
		final CloudGatekeeper cloudGatekeeper = new CloudGatekeeper(new Cloud(), address, port, uri, null);
		
		when(cloudGatekeeperRepository.findByCloud(any())).thenReturn(Optional.ofNullable(null));
		when(cloudGatekeeperRepository.findByAddressAndPortAndServiceUri(any(), anyInt(), any())).thenReturn(Optional.of(cloudGatekeeper));
		
		gatekeeperDBService.registerGatekeeper(new Cloud(), address, port, uri, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of updateGatekeeper
	
	@Test(expected = InvalidParameterException.class)
	public void testUpdateGatekeeperWithNullGatekeeper() {		
		gatekeeperDBService.updateGatekeeper(null, "1.1.1.1", 1000, "/testuri", "fewrdc");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateGatekeeperWithNullAddress() {		
		final CloudGatekeeper cloudGatekeeper = new CloudGatekeeper(new Cloud(), "1.1.1.1", 1000, "/testuri", "fewrdc");
		gatekeeperDBService.updateGatekeeper(cloudGatekeeper, null, 2000, "/anothertesturi", "trwaqre");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateGatekeeperWithBlankAddress() {		
		final CloudGatekeeper cloudGatekeeper = new CloudGatekeeper(new Cloud(), "1.1.1.1", 1000, "/testuri", "fewrdc");
		gatekeeperDBService.updateGatekeeper(cloudGatekeeper, "", 2000, "/anothertesturi", "trwaqre");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateGatekeeperWithInvalidMinPort() {		
		final CloudGatekeeper cloudGatekeeper = new CloudGatekeeper(new Cloud(), "1.1.1.1", 1000, "/testuri", "fewrdc");
		gatekeeperDBService.updateGatekeeper(cloudGatekeeper, "2.2.2.2", CommonConstants.SYSTEM_PORT_RANGE_MIN - 1, "/anothertesturi", "trwaqre");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateGatekeeperWithInvalidMaxPort() {		
		final CloudGatekeeper cloudGatekeeper = new CloudGatekeeper(new Cloud(), "1.1.1.1", 1000, "/testuri", "fewrdc");
		gatekeeperDBService.updateGatekeeper(cloudGatekeeper, "2.2.2.2", CommonConstants.SYSTEM_PORT_RANGE_MAX + 1, "/anothertesturi", "trwaqre");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateGatekeeperWithNullUri() {		
		final CloudGatekeeper cloudGatekeeper = new CloudGatekeeper(new Cloud(), "1.1.1.1", 1000, "/testuri", "fewrdc");
		gatekeeperDBService.updateGatekeeper(cloudGatekeeper, "2.2.2.2", 2000, null, "trwaqre");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateGatekeeperWithBlankUri() {		
		final CloudGatekeeper cloudGatekeeper = new CloudGatekeeper(new Cloud(), "1.1.1.1", 1000, "/testuri", "fewrdc");
		gatekeeperDBService.updateGatekeeper(cloudGatekeeper, "2.2.2.2", 2000, "", "trwaqre");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateGatekeeperWithSecureCloudButhWithoutAuthInfo() {	
		final Cloud cloud = new Cloud();
		cloud.setSecure(true);
		final CloudGatekeeper cloudGatekeeper = new CloudGatekeeper( cloud, "1.1.1.1", 1000, "/testuri", "fewrdc");
		gatekeeperDBService.updateGatekeeper(cloudGatekeeper, "2.2.2.2", 2000, "/testUri", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateGatekeeperWithSecureCloudButhWithBlankAuthInfo() {	
		final Cloud cloud = new Cloud();
		cloud.setSecure(true);
		final CloudGatekeeper cloudGatekeeper = new CloudGatekeeper( cloud, "1.1.1.1", 1000, "/testuri", "fewrdc");
		gatekeeperDBService.updateGatekeeper(cloudGatekeeper, "2.2.2.2", 2000, "/testUri", "");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateGatekeeperWithAlreadyExistingAdrressPortUriCombination() {
		final String address = "1.1.1.1";
		final int port = 1000;
		final String uri = "/testuri";
		final CloudGatekeeper existingGatekeeper = new CloudGatekeeper(new Cloud(), address, port, uri, null);
		
		when(cloudGatekeeperRepository.findByCloud(any())).thenReturn(Optional.ofNullable(null));
		when(cloudGatekeeperRepository.findByAddressAndPortAndServiceUri(any(), anyInt(), any())).thenReturn(Optional.of(existingGatekeeper));
		
		gatekeeperDBService.updateGatekeeper(existingGatekeeper, "another address", port, uri, null);
	}
}
