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
		normalizeSystemRequestDTO(request);
		
		return converted.equals(request);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private DTOUtilities() {
		throw new UnsupportedOperationException();
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