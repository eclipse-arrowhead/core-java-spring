/********************************************************************************
 * Copyright (c) 2020 {Lulea University of Technology}
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors: 
 *   {Lulea University of Technology} - implementation
 *   Arrowhead Consortia - conceptualization 
 ********************************************************************************/

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataManagerOperationDTO implements Serializable {

    //=================================================================================================
    // members
    
    private static final long serialVersionUID = 2528826741322136689L;
    
    private String op;
    private String serviceName;
    private String serviceType;
            
    //=================================================================================================
    // methods
    
    //-------------------------------------------------------------------------------------------------
    public DataManagerOperationDTO() {}
    
    //-------------------------------------------------------------------------------------------------
    public String getOp() { return op; }
    public String getServiceName() { return serviceName; }
    public String getServiceType() { return serviceType; }

    //-------------------------------------------------------------------------------------------------
    public void setOp(final String op) { this.op = new String(op); }
    public void setServiceName(final String serviceName) { this.serviceName = new String(serviceName); }
    public void setServiceType(final String serviceType) { this.serviceType = new String(serviceType); }

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