package eu.arrowhead.common.dto.internal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

@RunWith(SpringRunner.class)
public class DTOUtilitiesTest {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void equalsSystemInResponseAndRequestBothNull() {
		Assert.assertEquals(true, DTOUtilities.equalsSystemInResponseAndRequest(null, null));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void equalsSystemInResponseAndRequestResponseNull() {
		Assert.assertEquals(false, DTOUtilities.equalsSystemInResponseAndRequest(null, new SystemRequestDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void equalsSystemInResponseAndRequestRequestNull() {
		Assert.assertEquals(false, DTOUtilities.equalsSystemInResponseAndRequest(new SystemResponseDTO(), null));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void equalsSystemInResponseAndRequestFullMatch() {
		final SystemRequestDTO request = new SystemRequestDTO();
		request.setSystemName("name");
		request.setAddress("localhost");
		request.setPort(1234);
		
		final SystemResponseDTO response = new SystemResponseDTO(1, "name", "localhost", 1234, null, null, null);
		
		Assert.assertEquals(true, DTOUtilities.equalsSystemInResponseAndRequest(response, request));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void equalsSystemInResponseAndRequestIngnoreCaseAndTrimMatch() {
		final SystemRequestDTO request = new SystemRequestDTO();
		request.setSystemName("Name");
		request.setAddress("localhost  ");
		request.setPort(1234);
		
		final SystemResponseDTO response = new SystemResponseDTO(1, " nAme", "\tlocalhost", 1234, null, null, null);
		
		Assert.assertEquals(true, DTOUtilities.equalsSystemInResponseAndRequest(response, request));
	}
}