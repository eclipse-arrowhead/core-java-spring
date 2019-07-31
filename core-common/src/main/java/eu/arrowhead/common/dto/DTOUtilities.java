package eu.arrowhead.common.dto;

public class DTOUtilities {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static boolean equalsSystemInResponseAndRequest(final SystemResponseDTO response, final SystemRequestDTO request) {
		if (response == null) {
			return request == null;
		}
		
		if (request == null) {
			return false;
		}
		
		final SystemRequestDTO converted = DTOConverter.convertSystemResponseDTOToSystemRequestDTO(response);
		normalizeSystemRequestDTO(converted);
		
		final SystemRequestDTO requestCopy = copySystemRequestDTO(request);
		normalizeSystemRequestDTO(requestCopy);
		
		return converted.equals(requestCopy);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private DTOUtilities() {
		throw new UnsupportedOperationException();
	}
	
	//-------------------------------------------------------------------------------------------------
	private static SystemRequestDTO copySystemRequestDTO(final SystemRequestDTO orig) {
		if (orig == null) {
			return null;
		}
		
		final SystemRequestDTO result = new SystemRequestDTO();
		result.setSystemName(orig.getSystemName());
		result.setAddress(orig.getAddress());
		result.setPort(orig.getPort());
		result.setAuthenticationInfo(orig.getAuthenticationInfo());
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static void normalizeSystemRequestDTO(final SystemRequestDTO dto) {
		final String systemName = dto.getSystemName();
		if (systemName != null) {
			dto.setSystemName(systemName.toLowerCase().trim());
		}
		
		final String address = dto.getAddress();
		if (address != null) {
			dto.setAddress(address.toLowerCase().trim());
		}
	}
}