package eu.arrowhead.common.database.repository.impl;

import java.time.ZonedDateTime;

import javax.persistence.EntityManager;

import eu.arrowhead.common.database.entity.ProcessableEntity;
import eu.arrowhead.core.gams.dto.ProcessingState;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public abstract class ProcessableEntitySpecification<T extends ProcessableEntity> extends SimpleJpaRepository<T, Long> {

    protected static final String ID = "id";
    protected static final String CREATED_AT = "createdAt";
    protected static final String STATE = "state";
    protected static final String VALID_TILL = "validTill";

    public ProcessableEntitySpecification(final Class<T> domainClass, final EntityManager em) {
        super(domainClass, em);
    }

    protected Specification<T> stateIsSpec(final ProcessingState state) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(STATE), state);
    }

    protected Specification<T> validSpec() {
        return (root, query, builder) -> {
            final ZonedDateTime now = ZonedDateTime.now();
            return builder.greaterThan(root.get(VALID_TILL), now);
        };
    }

    protected Sort sort() {
        return Sort.by(Sort.Direction.DESC, CREATED_AT);
    }
}
