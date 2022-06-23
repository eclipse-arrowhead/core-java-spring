package eu.arrowhead.core.gams.service;

import java.util.Iterator;

import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.core.gams.GamsProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventWorker implements Runnable {
    private final Logger logger = LogManager.getLogger();
    private final GamsProperties properties;
    private final EventService eventService;
    private final EventConsumer consumer;

    public EventWorker(final GamsProperties properties, final EventService eventService, final EventConsumer consumer) {
        this.properties = properties;
        this.eventService = eventService;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        logger.debug("Loading up to {} events", properties.getBatchSize());
        final Iterable<Event> events = eventService.loadEvent(properties.getBatchSize());
        final Iterator<Event> iterator = events.iterator();

        try {
            while (iterator.hasNext()) {
                consumer.processEvent(iterator.next());
            }
        } catch (final Exception e) {
            logger.fatal("Unknown exception during events worker run: {}: {}", e.getClass().getSimpleName(), e.getMessage());
        } finally {
            iterator.forEachRemaining(eventService::persisted);
        }
    }
}
