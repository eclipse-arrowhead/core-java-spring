package eu.arrowhead.core.mscv.service;

import java.util.Optional;
import javax.persistence.PersistenceException;

import eu.arrowhead.common.database.entity.mscv.Standard;
import eu.arrowhead.common.database.repository.mscv.StandardRepository;
import eu.arrowhead.common.exception.ArrowheadException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static eu.arrowhead.core.mscv.Validation.PAGE_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.STANDARD_NULL_ERROR_MESSAGE;

@Service
public class StandardService {

    private final Logger logger = LogManager.getLogger();
    private final StandardRepository repository;

    @Autowired
    public StandardService(final StandardRepository repository) {this.repository = repository;}


    @Transactional
    public Standard create(final Standard standard) {
        try {
            logger.debug("create({}) started", standard);
            Assert.notNull(standard, STANDARD_NULL_ERROR_MESSAGE);
            return repository.saveAndFlush(standard);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to create Standard", pe);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Standard> findByIdentification(final String identification) {
        try {
            logger.debug("find({}) started", identification);
            Assert.hasText(identification, STANDARD_NULL_ERROR_MESSAGE);

            return repository.findByIdentification(identification.trim());
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to find Standard", pe);
        }
    }

    @Transactional(readOnly = true)
    public boolean exists(final Standard standard) {
        try {
            logger.debug("exists({}) started", standard);
            Assert.notNull(standard, STANDARD_NULL_ERROR_MESSAGE);

            return repository.exists(Example.of(standard, ExampleMatcher.matchingAll()));
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to verify Standard exists", pe);
        }
    }

    @Transactional(readOnly = true)
    public Page<Standard> pageAll(final Pageable pageable) {
        try {
            logger.debug("pageAll({}) started", pageable);
            Assert.notNull(pageable, PAGE_NULL_ERROR_MESSAGE);
            return repository.findAll(pageable);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to query all Standards", pe);
        }
    }

    @Transactional
    public Standard replace(final Standard oldStandard, final Standard newStandard) {
        try {
            logger.debug("replace({},{}) started", oldStandard, newStandard);
            Assert.notNull(oldStandard, "old " + STANDARD_NULL_ERROR_MESSAGE);
            Assert.notNull(newStandard, "new " + STANDARD_NULL_ERROR_MESSAGE);

            oldStandard.setName(newStandard.getName());
            return repository.saveAndFlush(oldStandard);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to replace Standard", pe);
        }
    }

    @Transactional
    public void delete(final String identification) {
        try {
            logger.debug("delete({}) started", identification);
            Assert.hasText(identification, STANDARD_NULL_ERROR_MESSAGE);

            final Optional<Standard> optionalStandard = findByIdentification(identification);
            optionalStandard.ifPresent(repository::delete);
            repository.flush();
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to delete Standard", pe);
        }
    }
}