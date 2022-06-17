package eu.arrowhead.core.mscv.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.persistence.PersistenceException;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.mscv.Mip;
import eu.arrowhead.common.database.entity.mscv.Script;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.entity.mscv.VerificationEntry;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.entity.mscv.VerificationResult;
import eu.arrowhead.common.database.entity.mscv.VerificationResultDetail;
import eu.arrowhead.common.database.repository.mscv.VerificationEntryListRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationEntryRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationExecutionDetailRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationExecutionRepository;
import eu.arrowhead.common.database.view.mscv.VerificationListView;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.common.dto.shared.mscv.MipVerificationResultDto;
import eu.arrowhead.common.dto.shared.mscv.VerificationResultDto;
import eu.arrowhead.common.dto.shared.mscv.VerificationResultListResponseDto;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.mscv.MscvDefaults;
import eu.arrowhead.core.mscv.MscvDtoConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;
import static eu.arrowhead.core.mscv.Validation.ID_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.LAYER_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.NAME_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.PAGE_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.TARGET_NULL_ERROR_MESSAGE;

@Service
public class VerificationService {

    private static final String DEFAULT_LIST_DESCRIPTION = "An automatically created default list";

    private final Logger logger = LogManager.getLogger();
    private final VerificationExecutionDetailRepository executionDetailRepo;
    private final VerificationExecutionRepository executionRepo;
    private final VerificationEntryListRepository verListRepository;
    private final VerificationEntryRepository verEntryRepository;
    private final ScriptService scriptService;
    private final MscvDefaults defaults;

    @Autowired
    public VerificationService(final VerificationExecutionDetailRepository executionDetailRepo,
                               final VerificationExecutionRepository executionRepo,
                               final VerificationEntryListRepository verListRepository,
                               final VerificationEntryRepository verEntryRepository,
                               final ScriptService scriptService,
                               final MscvDefaults defaults) {
        super();
        this.executionDetailRepo = executionDetailRepo;
        this.executionRepo = executionRepo;
        this.verListRepository = verListRepository;
        this.verEntryRepository = verEntryRepository;
        this.scriptService = scriptService;
        this.defaults = defaults;
    }

    @Transactional(readOnly = true)
    public VerificationListView getListById(final Long id) {
        logger.debug("getListById({}) started", id);
        Assert.notNull(id, ID_NULL_ERROR_MESSAGE);
        return MscvDtoConverter.convertToView(findListById(id));
    }

    @Transactional(readOnly = true)
    public VerificationListView getListByNameAndLayer(final String name, final Layer layer) {
        try {
            logger.debug("getListByName({}, {}) started", name, layer);
            Assert.hasText(name, NAME_NULL_ERROR_MESSAGE);
            Assert.notNull(layer, LAYER_NULL_ERROR_MESSAGE);
            final Optional<VerificationEntryList> optional = verListRepository.findOneByNameAndLayer(name, layer);
            return MscvDtoConverter.convertToView(optional.orElseThrow(notFoundException("Verification list name")));
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to find VerificationList", pe);
        }
    }

    @Transactional(readOnly = true)
    public VerificationResultDto getDetailResults(final Target target, final Layer layer) {
        try {
            logger.debug("getDetailResults({},{}) started", target, layer);
            Assert.notNull(target, TARGET_NULL_ERROR_MESSAGE);
            Assert.notNull(layer, LAYER_NULL_ERROR_MESSAGE);
            final Optional<VerificationResult> optional = executionRepo.findTopByTargetAndVerificationListLayerOrderByExecutionDateDesc(target, layer);
            final VerificationResult result = optional.orElseThrow(notFoundException("Verification Detail Results"));
            return createResults(result);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to find VerificationResult", pe);
        }
    }

    @Transactional(readOnly = true)
    public VerificationResultListResponseDto findDetailResults(final Pageable page, final Target target,
                                                               final ZonedDateTime from, final ZonedDateTime to,
                                                               final List<VerificationEntryList> list) {
        try {
            logger.debug("findDetailResults({},{},{},{},{}) started", page, target, from, to, list);
            Assert.notNull(page, PAGE_NULL_ERROR_MESSAGE);
            Assert.notNull(target, TARGET_NULL_ERROR_MESSAGE);
            // other parameters are optional

            final ZonedDateTime validatedTo = Objects.isNull(to) ? ZonedDateTime.now() : to;
            final ZonedDateTime validatedFrom = Objects.isNull(from) ? validatedTo.minusYears(10) : from;

            final Page<VerificationResult> executions =
                    executionRepo.findAllByTargetAndVerificationListInAndExecutionDateIsBetween(page, target, list, validatedFrom, validatedTo);

            final List<VerificationResultDto> content = new ArrayList<>();

            for (final VerificationResult execution : executions) {
                content.add(createResults(execution));
            }

            final Page<VerificationResultDto> resultsPage = new PageImpl<>(content, page, executions.getSize());
            return new VerificationResultListResponseDto(resultsPage);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to find VerificationResultList", pe);
        }
    }

    @Transactional(readOnly = true)
    public List<VerificationEntryList> findListByProbe(final VerificationEntryList probe) {
        try {
            return verListRepository.findAll(Example.of(probe, ExampleMatcher.matchingAny()));
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to find VerificationEntryLists", pe);
        }
    }

    @Transactional(readOnly = true)
    public VerificationEntryList findListById(final Long id) {
        try {
            logger.debug("findListById({}) started", id);
            Assert.notNull(id, ID_NULL_ERROR_MESSAGE);
            final Optional<VerificationEntryList> optional = verListRepository.findById(id);
            return optional.orElseThrow(notFoundException("Verification list ID"));
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to find VerificationEntryList", pe);
        }
    }

    @Transactional
    public VerificationEntryList findOrCreateSuitableList(final Target target, final Layer layer) {
        try {
            logger.debug("findSuitableList({}) started", target);
            Assert.notNull(target, TARGET_NULL_ERROR_MESSAGE);
            Assert.notNull(layer, LAYER_NULL_ERROR_MESSAGE);

            // no strategy exists yet. using default list
            Optional<VerificationEntryList> defaultList = verListRepository.findOneByNameAndLayer(defaults.getListName(), layer);
            //return defaultList.orElseThrow(() -> new MscvException("No suitable list found"));
            return defaultList.orElseGet(() -> createDefaultList(layer));
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to find or create suitable VerificationEntryList", pe);
        }
    }

    private VerificationEntryList createDefaultList(final Layer layer) {
        logger.debug("createDefaultList({}) started", layer);
        final Set<Script> allScriptsByLayer = scriptService.findAllByLayer(layer);

        final var unsavedEntryList = new VerificationEntryList(defaults.getListName(),
                                                               DEFAULT_LIST_DESCRIPTION,
                                                               defaults.getVerificationInterval());
        final Set<VerificationEntry> unsavedEntries = new HashSet<>();
        final VerificationEntryList returnValue = verListRepository.saveAndFlush(unsavedEntryList);

        for (final Script script : allScriptsByLayer) {
            unsavedEntries.add(new VerificationEntry(script.getMip(), defaults.getMipWeight(), returnValue));
        }

        final List<VerificationEntry> entries = verEntryRepository.saveAll(unsavedEntries);
        returnValue.setEntries(new HashSet<>(entries));
        return returnValue;
    }

    private VerificationResultDto createResults(final VerificationResult execution) {
        final List<VerificationResultDetail> details = executionDetailRepo.findAllByExecution(execution);

        final VerificationResultDto retValue = new VerificationResultDto();
        retValue.setExecutionDate(Utilities.convertZonedDateTimeToUTCString(execution.getExecutionDate()));
        retValue.setListName(execution.getVerificationList().getName());
        retValue.setListDescription(execution.getVerificationList().getDescription());
        retValue.setTarget(MscvDtoConverter.convert(execution.getTarget()));
        retValue.setSuccessIndicator(execution.getResult());
        retValue.setResult(calculatedWeightedAverage(details));
        retValue.setMipResults(convert(details));

        return retValue;
    }

    private List<MipVerificationResultDto> convert(final List<VerificationResultDetail> details) {
        final List<MipVerificationResultDto> retValue = new ArrayList<>();

        for (VerificationResultDetail detail : details) {
            final Mip mip = detail.getVerificationEntry().getMip();
            final var dto = new MipVerificationResultDto();
            dto.setCategory(mip.getCategory().getAbbreviation());
            dto.setStandard(mip.getStandard().getIdentification());
            dto.setDomain(mip.getDomain().getName());
            dto.setLayer(detail.getScript().getLayer());
            dto.setSuccessIndicator(detail.getResult());
            dto.setMipName(mip.getName());
            dto.setMipName(mip.getIdentifier());
            retValue.add(dto);
        }

        return retValue;
    }

    private double calculatedWeightedAverage(final List<VerificationResultDetail> details) {
        int sumOfWeight = 0;
        int score = 0; // score as percentage value

        for (final VerificationResultDetail detail : details) {
            final VerificationEntry entry = detail.getVerificationEntry();
            switch (detail.getResult()) {
                case SUCCESS:
                case NOT_APPLICABLE:
                    sumOfWeight += entry.getWeight();
                    score += 100;
                    break;
                case NO_SUCCESS:
                case ERROR:
                    sumOfWeight += entry.getWeight();
                    score += 0;
                    break;
                case IN_PROGRESS:
                    // intentionally empty
                default:
                    break;
            }
        }

        // round to 2 digit precision
        long scoreX100 = Math.round(score * 100.0 / sumOfWeight);
        return scoreX100 / 100.0;
    }
}
