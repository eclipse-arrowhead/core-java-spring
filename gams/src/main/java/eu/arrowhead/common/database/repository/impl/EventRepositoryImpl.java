package eu.arrowhead.common.database.repository.impl;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.ProcessableEntity;
import eu.arrowhead.common.database.repository.EventRepository;
import eu.arrowhead.core.gams.dto.ProcessingState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.stereotype.Repository;

import static org.springframework.data.jpa.domain.Specification.where;

@Repository
public class EventRepositoryImpl extends ProcessableEntitySpecification<Event> implements EventRepository {

    protected static final String TYPE = "type";
    protected static final String INSTANCE_ID = "instanceId";
    protected static final String NAME = "name";
    protected static final String SOURCE = "source";
    protected static final String DATA = "data";
    protected static final String VALID_FROM = "validFrom";

    private final Logger logger = LogManager.getLogger();
    private final EntityManager entityManager;

    @Autowired
    public EventRepositoryImpl(final JpaContext context) {
        super(Event.class, context.getEntityManagerByManagedType(Event.class));
        this.entityManager = context.getEntityManagerByManagedType(Event.class);
    }

    @Transactional
    public Optional<Event> findValidEvent(final ProcessingState previousState, final ProcessingState newState) {

        final List<Event> validEvents = findValidEvent(previousState, newState, 1);
        if (validEvents.size() == 0) {
            return Optional.empty();
        } else if (validEvents.size() == 1) {
            return Optional.of(validEvents.get(0));
        } else {
            throw new PersistenceException("Unknown error during event query");
        }
    }

    @Transactional
    public List<Event> findValidEvent(final ProcessingState previousState,
                                      final ProcessingState newState, int limit) {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaUpdate<Event> query = builder.createCriteriaUpdate(Event.class);
        final Root<Event> root = query.from(Event.class);

        final Specification<Event> specification = where(stateIsSpec(previousState).and(validSpec()));
        final PageRequest pageRequest = PageRequest.of(0, limit, Sort.Direction.DESC, CREATED_AT);
        final Page<Event> events = findAll(specification, pageRequest);

        final List<Event> content = events.getContent();

        if (!content.isEmpty()) {
            final List<Long> contentIds = content.stream().map(ProcessableEntity::getId).collect(Collectors.toList());

            query.set(STATE, newState).where(root.get(ID).in(contentIds));
            entityManager.createQuery(query).executeUpdate();

            for (final Event event : content) {
                refresh(event);
            }
        }
        return content;
    }

    @Transactional
    public Iterable<Event> findAllValidEvents(final ProcessingState state) {
        return findAll(where(stateIsSpec(state)).and(validSpec()), sort());
    }

    @Transactional
    public void expireEvents() {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaUpdate<Event> query = builder.createCriteriaUpdate(Event.class);
        final Root<Event> root = query.from(Event.class);

        final List<Event> expiredEvents = findAll(where(expiredSpec()).and(notDoneSpec()));

        final int plannedCount = expiredEvents.size();
        logger.info("A total of {} events expired", plannedCount);

        if (!expiredEvents.isEmpty()) {
            final List<Long> expiredIds = new ArrayList<>();

            expiredEvents.forEach((e) -> {
                logger.debug("Event expired: {}", e);
                expiredIds.add(e.getId());
            });

            query.set(STATE, ProcessingState.EXPIRED).where(root.get(ID).in(expiredIds));
            final int count = entityManager.createQuery(query).executeUpdate();

            if (count != plannedCount) {
                logger.warn("{} events planned, but only {} events updated in database", plannedCount, count);
            }
        }
    }

    @Transactional
    public Long countValid() {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> query = builder.createQuery(Long.class);
        final Root<Event> root = query.from(Event.class);

        final Specification<Event> specification = where(notDoneSpec().and(validSpec()));

        query.select(builder.count(root))
             .where(specification.toPredicate(root, query, builder));
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public void refresh(final Event event) {
        entityManager.refresh(event);
    }

    @Override
    protected Specification<Event> validSpec() {
        return (root, query, builder) -> {
            final ZonedDateTime now = ZonedDateTime.now();
            final Predicate validFrom = builder.lessThanOrEqualTo(root.get(VALID_FROM), now);
            final Predicate validTo = builder.greaterThan(root.get(VALID_TILL), now);
            return builder.and(validFrom, validTo);
        };
    }

    protected Specification<Event> expiredSpec() {
        return (root, query, builder) -> {
            final ZonedDateTime now = ZonedDateTime.now();
            return builder.lessThan(root.get(VALID_TILL), now);
        };
    }

    protected Specification<Event> instanceSpec(final GamsInstance instance) {
        return (root, query, builder) -> {
            final Join<Event, GamsInstance> join = root.join(INSTANCE_ID, JoinType.INNER);
            return builder.equal(join.get(ID), instance.getId());
        };
    }

    protected Specification<Event> notDoneSpec() {
        return (root, query, builder) -> builder.notEqual(root.get(STATE), ProcessingState.PROCESSED);
    }

    protected Sort sort() {
        return Sort.by(Sort.Direction.DESC, VALID_FROM);
    }
}
