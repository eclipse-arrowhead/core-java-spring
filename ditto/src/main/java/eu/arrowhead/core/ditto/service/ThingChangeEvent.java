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

package eu.arrowhead.core.ditto.service;

import java.util.Objects;
import org.eclipse.ditto.client.changes.ThingChange;
import org.springframework.context.ApplicationEvent;

public class ThingChangeEvent extends ApplicationEvent {
	private final ThingChange change;

	public ThingChangeEvent(final Object source, final ThingChange change) {
		super(Objects.requireNonNull(source));
		this.change = Objects.requireNonNull(change);
	}

	public ThingChange getChange() {
		return change;
	}
}
