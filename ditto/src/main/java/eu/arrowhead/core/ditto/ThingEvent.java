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

package eu.arrowhead.core.ditto;

import org.eclipse.ditto.things.model.Thing;
import org.springframework.context.ApplicationEvent;
import org.springframework.util.Assert;

public class ThingEvent extends ApplicationEvent {

	//=================================================================================================
	// members

	private final Thing thing;
	private final ThingEventType type;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ThingEvent(final Object source, final Thing thing, final ThingEventType type) {
		super(source);

		Assert.notNull(source, "source is null");
		Assert.notNull(type, "type is null");

		this.thing = thing;
		this.type = type;
	}

	//-------------------------------------------------------------------------------------------------
	public Thing getThing() {
		return thing;
	}

	//-------------------------------------------------------------------------------------------------
	public ThingEventType getType() {
		return type;
	}

}
