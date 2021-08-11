package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;

public class ChoreographerSessionStepResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -7338823471311785589L;
	
	private long id;
	private ChoreographerSessionResponseDTO session;
	private ChoreographerStepResponseDTO step;
	private ChoreographerExecutorResponseDTO executor;
	private ChoreographerSessionStepStatus status;
	private String message;
    private String createdAt;
    private String updatedAt;
    
    //=================================================================================================
    // methods
	
    //-------------------------------------------------------------------------------------------------
    public ChoreographerSessionStepResponseDTO() {}
    		
    //-------------------------------------------------------------------------------------------------
	public ChoreographerSessionStepResponseDTO(final long id, final ChoreographerSessionResponseDTO session, final ChoreographerStepResponseDTO step,
											   final ChoreographerExecutorResponseDTO executor, final ChoreographerSessionStepStatus status, final String message,
											   final String createdAt, final String updatedAt) {
		this.id = id;
		this.session = session;
		this.step = step;
		this.executor = executor;
		this.status = status;
		this.message = message;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public ChoreographerSessionResponseDTO getSession() { return session; }
	public ChoreographerStepResponseDTO getStep() { return step; }
	public ChoreographerExecutorResponseDTO getExecutor() { return executor; }
	public ChoreographerSessionStepStatus getStatus() { return status; }
	public String getMessage() { return message; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setSession(final ChoreographerSessionResponseDTO session) { this.session = session; }
	public void setStep(final ChoreographerStepResponseDTO step) { this.step = step; }
	public void setExecutor(final ChoreographerExecutorResponseDTO executor) { this.executor = executor; }
	public void setStatus(final ChoreographerSessionStepStatus status) { this.status = status; }
	public void setMessage(final String message) { this.message = message; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
    	try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
    }
}
