package eu.arrowhead.core.ditto.service;

import org.eclipse.ditto.client.changes.ThingChange;
import org.springframework.context.ApplicationEvent;

public class ThingEvent extends ApplicationEvent {
	private ThingChange change;

	public ThingEvent(final Object source, final ThingChange change) {
			super(source);
			this.change = change;
	}
	public ThingChange getChange() {
			return change;
	}
}
