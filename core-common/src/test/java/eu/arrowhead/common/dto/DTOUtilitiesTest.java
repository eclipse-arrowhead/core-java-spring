package eu.arrowhead.common.dto;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.exception.TimeoutException;
import eu.arrowhead.common.exception.UnavailableServerException;

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
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateExceptionFromErrorMessageDTOParamNull() {
		DTOUtilities.createExceptionFromErrorMessageDTO(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateExceptionFromErrorMessageDTOExceptionTypeNull() {
		DTOUtilities.createExceptionFromErrorMessageDTO(new ErrorMessageDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCreateExceptionFromErrorMessageDTOArrowheadException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.ARROWHEAD, "origin");
		DTOUtilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testCreateExceptionFromErrorMessageDTOAuthException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.AUTH, "origin");
		DTOUtilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testCreateExceptionFromErrorMessageDTOBadPayloadException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.BAD_PAYLOAD, "origin");
		DTOUtilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateExceptionFromErrorMessageDTOInvalidParameterException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.INVALID_PARAMETER, "origin");
		DTOUtilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testCreateExceptionFromErrorMessageDTODataNotFoundException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.DATA_NOT_FOUND, "origin");
		DTOUtilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCreateExceptionFromErrorMessageDTOGenericException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.GENERIC, "origin");
		DTOUtilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = TimeoutException.class)
	public void testCreateExceptionFromErrorMessageDTOTimeoutException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.TIMEOUT, "origin");
		DTOUtilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = UnavailableServerException.class)
	public void testCreateExceptionFromErrorMessageDTOUnavailableServerException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.UNAVAILABLE, "origin");
		DTOUtilities.createExceptionFromErrorMessageDTO(error);
	}
}