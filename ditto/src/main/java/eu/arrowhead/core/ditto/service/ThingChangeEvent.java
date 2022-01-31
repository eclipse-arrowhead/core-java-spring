package eu.arrowhead.core.ditto.service;

import java.util.Objects;
import org.eclipse.ditto.client.changes.ThingChange;
import org.springframework.context.ApplicationEvent;

public class ThingChangeEvent extends ApplicationEvent {
	private ThingChange change;

	public ThingChangeEvent(final Object source, final ThingChange change) {
		super(Objects.requireNonNull(source));
		this.change = Objects.requireNonNull(change);
	}

	public ThingChange getChange() {
		return change;
	}
}
