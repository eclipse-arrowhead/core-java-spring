/********************************************************************************
 * Copyright (c) 2020 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;

public class ChoreographerRunPlanRequestDTO implements Serializable {


	//=================================================================================================
    // members
	
    private static final long serialVersionUID = 270117099471203107L;
	
    private Long planId;
    private long quantity = 1;
    
    private boolean allowInterCloud = false; // if true, then providers (for steps and for executor's dependencies) can come from other clouds, too
    private boolean chooseOptimalExecutor = false; // if true, executor selection tries to minimalize inter-cloud connections (but executor selection takes longer)
    
    // notify URI parts
    private String notifyProtocol = CommonConstants.HTTP; // http or https
    private String notifyAddress; // if address is not set, then notify is disabled
    private int notifyPort = 80;
    private String notifyPath = "";

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public Long getPlanId() { return planId; }
    public long getQuantity() { return quantity; }
	public boolean isAllowInterCloud() { return allowInterCloud; }
    public boolean getChooseOptimalExecutor() { return chooseOptimalExecutor; }
    public String getNotifyProtocol() { return notifyProtocol; }
	public String getNotifyAddress() { return notifyAddress; }
	public int getNotifyPort() { return notifyPort; }
	public String getNotifyPath() { return notifyPath; }

	//-------------------------------------------------------------------------------------------------
    public void setPlanId(final Long planId) { this.planId = planId; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
    public void setAllowInterCloud(final boolean allowInterCloud) { this.allowInterCloud = allowInterCloud; }
    public void setChooseOptimalExecutor(final boolean chooseOptimalExecutor) { this.chooseOptimalExecutor = chooseOptimalExecutor; }
    public void setNotifyProtocol(final String notifyProtocol) { this.notifyProtocol = notifyProtocol; }
    public void setNotifyAddress(final String notifyAddress) { this.notifyAddress = notifyAddress; }
    public void setNotifyPort(final int notifyPort) { this.notifyPort = notifyPort; }
    public void setNotifyPath(final String notifyPath) { this.notifyPath = notifyPath; }
    
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