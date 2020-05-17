package eu.arrowhead.core.mscv.service;

import java.util.Optional;

import eu.arrowhead.common.database.entity.mscv.Mip;
import eu.arrowhead.common.database.repository.mscv.MipRepository;
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

import static eu.arrowhead.core.mscv.Validation.ABBR_EMPTY_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.CATEGORY_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.EXAMPLE_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.ID_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.MIP_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.PAGE_NULL_ERROR_MESSAGE;

@Service
public class MipService {

    private final Logger logger = LogManager.getLogger();
    private final MipRepository repository;

    @Autowired
    public MipService(final MipRepository repository) {this.repository = repository;}


    @Transactional
    public Mip create(final Mip mip) {
        logger.debug("create({}) started", mip);
        Assert.notNull(mip, MIP_NULL_ERROR_MESSAGE);
        return repository.saveAndFlush(mip);
    }

    @Transactional(readOnly = true)
    public Optional<Mip> findByExternalIdAndCategory(final Integer extId, final String category) {
        logger.debug("findByExternalIdAndCategory({},{}) started", extId, category);
        Assert.notNull(extId, ID_NULL_ERROR_MESSAGE);
        Assert.notNull(category, CATEGORY_NULL_ERROR_MESSAGE);
        return repository.findByExtIdAndCategoryName(extId, category);
    }

    @Transactional(readOnly = true)
    public Optional<Mip> findByExternalIdAndCategoryAbbreviation(final Integer extId, final String categoryAbbreviation) {
        logger.debug("findByExternalIdAndCategory({},{}) started", extId, categoryAbbreviation);
        Assert.notNull(extId, ID_NULL_ERROR_MESSAGE);
        Assert.notNull(categoryAbbreviation, CATEGORY_NULL_ERROR_MESSAGE);
        return repository.findByExtIdAndCategoryAbbreviation(extId, categoryAbbreviation);
    }

    @Transactional(readOnly = true)
    public boolean exists(final Mip mip) {
        logger.debug("exists({}) started", mip);
        Assert.notNull(mip, MIP_NULL_ERROR_MESSAGE);
        return repository.exists(Example.of(mip, ExampleMatcher.matchingAll()));
    }

    @Transactional(readOnly = true)
    public Page<Mip> pageAll(final Pageable pageable) {
        logger.debug("pageAll({}) started", pageable);
        Assert.notNull(pageable, PAGE_NULL_ERROR_MESSAGE);
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Mip> pageByExample(final Example<Mip> example, final Pageable pageable) {
        logger.debug("pageByExample({}{}) started", example, pageable);
        Assert.notNull(example, EXAMPLE_NULL_ERROR_MESSAGE);
        Assert.notNull(pageable, PAGE_NULL_ERROR_MESSAGE);
        return repository.findAll(example, pageable);
    }

    @Transactional
    public Mip replace(final Mip oldMip, final Mip newMip) {
        logger.debug("replace({},{}) started", newMip, newMip);
        Assert.notNull(newMip, "old " + CATEGORY_NULL_ERROR_MESSAGE);
        Assert.notNull(newMip, "new " + CATEGORY_NULL_ERROR_MESSAGE);

        oldMip.setExtId(newMip.getExtId());
        oldMip.setName(newMip.getName());
        oldMip.setDescription(newMip.getDescription());
        oldMip.setStandard(newMip.getStandard());
        oldMip.setDomain(newMip.getDomain());
        oldMip.setCategory(newMip.getCategory());
        return repository.saveAndFlush(oldMip);
    }

    @Transactional
    public void delete(final Integer extId, final String categoryAbbreviation) {
        logger.debug("delete({},{}) started", extId, categoryAbbreviation);
        Assert.notNull(extId, ID_NULL_ERROR_MESSAGE);
        Assert.notNull(categoryAbbreviation, ABBR_EMPTY_ERROR_MESSAGE);

        final Optional<Mip> optionalMip = findByExternalIdAndCategoryAbbreviation(extId, categoryAbbreviation);
        optionalMip.ifPresent(repository::delete);
        repository.flush();
    }

}