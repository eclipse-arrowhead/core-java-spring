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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

public class ThingRequestDTO implements Serializable {

	//=================================================================================================
	// members

	private String definition;
	private Map<String,Object> attributes;
	private Map<String,Object> features;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@JsonCreator
	public ThingRequestDTO(
		@JsonProperty("definition") final String definition,
		@JsonProperty("attributes") final Map<String, Object> attributes,
		@JsonProperty("features") final Map<String, Object> features
		) {
		Assert.notNull(definition, "definition is null");
		Assert.notNull(attributes, "attributes is null");
		Assert.notNull(features, "features is null");

		this.definition = definition;
		this.attributes = attributes;
		this.features = features;
	}

	//-------------------------------------------------------------------------------------------------
	public String getDefinition() { return definition; }
	public final Map<String, Object> getAttributes() { return attributes; }
	public final Map<String, Object> getFeatures() { return features; }

	//-------------------------------------------------------------------------------------------------
	public void setDefinition(final String definition) { this.definition = definition; }
	public final void setAttributes(final Map<String, Object> attributes) { this.attributes = attributes; }
	public final void setFeatures(final Map<String, Object> features) { this.features = features; }

}
