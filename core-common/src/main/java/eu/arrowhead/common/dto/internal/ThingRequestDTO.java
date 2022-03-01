/********************************************************************************
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.Map;
import org.springframework.util.Assert;

public class ThingRequestDTO implements Serializable {

	//=================================================================================================
	// members

	private String definition;
	private String policyId;
	private Map<String,Object> attributes;
	private Map<String,Object> features;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ThingRequestDTO(
		final String definition,
		final String policyId,
		final Map<String, Object> attributes,
		final Map<String, Object> features
		) {
		Assert.notNull(definition, "definition is null");
		Assert.notNull(attributes, "attributes is null");
		Assert.notNull(features, "features is null");

		this.definition = definition;
		this.policyId = policyId;
		this.attributes = attributes;
		this.features = features;
	}

	//-------------------------------------------------------------------------------------------------
	public String getDefinition() { return definition; }
	public String getPolicyId() { return policyId; }
	public final Map<String, Object> getAttributes() { return attributes; }
	public final Map<String, Object> getFeatures() { return features; }

	//-------------------------------------------------------------------------------------------------
	public void setDefinition(final String definition) { this.definition = definition; }
	public void setPolicyId(final String policyId) { this.policyId = policyId; }
	public final void setAttributes(final Map<String, Object> attributes) { this.attributes = attributes; }
	public final void setFeatures(final Map<String, Object> features) { this.features = features; }

}
