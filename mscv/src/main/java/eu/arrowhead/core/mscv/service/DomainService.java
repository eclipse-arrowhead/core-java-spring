package eu.arrowhead.core.mscv.service;

import java.util.Optional;
import javax.persistence.PersistenceException;

import eu.arrowhead.common.database.entity.mscv.MipDomain;
import eu.arrowhead.common.database.repository.mscv.MipDomainRepository;
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

import static eu.arrowhead.core.mscv.Validation.DOMAIN_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.NAME_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.PAGE_NULL_ERROR_MESSAGE;

@Service
public class DomainService {

    private final Logger logger = LogManager.getLogger();
    private final MipDomainRepository repository;

    @Autowired
    public DomainService(final MipDomainRepository repository) {this.repository = repository;}


    @Transactional
    public MipDomain create(final MipDomain domain) {
        try {
            logger.debug("create({}) started", domain);
            Assert.notNull(domain, DOMAIN_NULL_ERROR_MESSAGE);
            return repository.saveAndFlush(domain);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to create Domain", pe);
        }
    }

    @Transactional(readOnly = true)
    public Optional<MipDomain> find(final String name) {
        try {
            logger.debug("find({}) started", name);
            Assert.notNull(name, NAME_NULL_ERROR_MESSAGE);
            return repository.findByName(name);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to find Domain", pe);
        }
    }

    @Transactional(readOnly = true)
    public boolean exists(final MipDomain domain) {
        try {
            logger.debug("exists({}) started", domain);
            Assert.notNull(domain, DOMAIN_NULL_ERROR_MESSAGE);
            return repository.exists(Example.of(domain, ExampleMatcher.matchingAll()));
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to verify existence of Domain", pe);
        }
    }

    @Transactional(readOnly = true)
    public Page<MipDomain> pageAll(final Pageable pageable) {
        try {
            logger.debug("pageAll({}) started", pageable);
            Assert.notNull(pageable, PAGE_NULL_ERROR_MESSAGE);
            return repository.findAll(pageable);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to query Domain", pe);
        }
    }

    @Transactional
    public MipDomain replace(final MipDomain oldDomain, final MipDomain newDomain) {
        try {
            logger.debug("replace({},{}) started", oldDomain, newDomain);
            Assert.notNull(oldDomain, "old " + DOMAIN_NULL_ERROR_MESSAGE);
            Assert.notNull(newDomain, "new " + DOMAIN_NULL_ERROR_MESSAGE);

            oldDomain.setName(newDomain.getName());
            return repository.saveAndFlush(oldDomain);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to replace Domain", pe);
        }
    }

    @Transactional
    public void delete(final String name) {
        try {
            logger.debug("delete({}) started", name);
            Assert.notNull(name, NAME_NULL_ERROR_MESSAGE);

            final Optional<MipDomain> optionalMipDomain = find(name);
            optionalMipDomain.ifPresent(repository::delete);
            repository.flush();
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to delete Domain", pe);
        }
    }
}