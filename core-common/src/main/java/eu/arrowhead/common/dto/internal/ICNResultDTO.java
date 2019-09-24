package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;

@JsonInclude(Include.NON_NULL)
public class ICNResultDTO extends OrchestrationResponseDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -286883397386724556L;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ICNResultDTO() {
		super();
	}

	//-------------------------------------------------------------------------------------------------
	public ICNResultDTO(final List<OrchestrationResultDTO> response) {
		super(response);
	}
}