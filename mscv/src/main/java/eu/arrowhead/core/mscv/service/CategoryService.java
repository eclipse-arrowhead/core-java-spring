package eu.arrowhead.core.mscv.service;

import java.util.Optional;
import javax.persistence.PersistenceException;

import eu.arrowhead.common.database.entity.mscv.MipCategory;
import eu.arrowhead.common.database.repository.mscv.MipCategoryRepository;
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

import static eu.arrowhead.core.mscv.Validation.CATEGORY_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.NAME_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.PAGE_NULL_ERROR_MESSAGE;

@Service
public class CategoryService {

    private final Logger logger = LogManager.getLogger();
    private final MipCategoryRepository repository;

    @Autowired
    public CategoryService(final MipCategoryRepository repository) {this.repository = repository;}


    @Transactional
    public MipCategory create(final MipCategory category) {
        try {
            logger.debug("create({}) started", category);
            Assert.notNull(category, CATEGORY_NULL_ERROR_MESSAGE);
            return repository.saveAndFlush(category);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to create Category", pe);
        }
    }

    @Transactional(readOnly = true)
    public Optional<MipCategory> find(final String name) {
        try {
            logger.debug("find({}) started", name);
            Assert.notNull(name, NAME_NULL_ERROR_MESSAGE);

            return repository.findByName(name);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to find Category", pe);
        }
    }

    @Transactional(readOnly = true)
    public boolean exists(final MipCategory category) {
        try {
            logger.debug("exists({}) started", category);
            Assert.notNull(category, CATEGORY_NULL_ERROR_MESSAGE);

            return repository.exists(Example.of(category, ExampleMatcher.matchingAll()));
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to verify existence of Category", pe);
        }
    }

    @Transactional(readOnly = true)
    public Page<MipCategory> pageAll(final Pageable pageable) {
        try {
            logger.debug("pageAll({}) started", pageable);
            Assert.notNull(pageable, PAGE_NULL_ERROR_MESSAGE);
            return repository.findAll(pageable);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to query Category", pe);
        }
    }

    @Transactional
    public MipCategory replace(final MipCategory oldCategory, final MipCategory newCategory) {
        try {
            logger.debug("replace({},{}) started", oldCategory, newCategory);
            Assert.notNull(oldCategory, "old " + CATEGORY_NULL_ERROR_MESSAGE);
            Assert.notNull(newCategory, "new " + CATEGORY_NULL_ERROR_MESSAGE);

            oldCategory.setName(newCategory.getName());
            oldCategory.setAbbreviation(newCategory.getAbbreviation());
            return repository.saveAndFlush(oldCategory);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to replace Category", pe);
        }
    }

    @Transactional
    public void delete(final String name) {
        try {
            logger.debug("delete({}) started", name);
            Assert.notNull(name, NAME_NULL_ERROR_MESSAGE);

            final Optional<MipCategory> optionalMipCategory = find(name);
            optionalMipCategory.ifPresent(repository::delete);
            repository.flush();
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to delete Category", pe);
        }
    }
}