package eu.arrowhead.core.mscv.service;

import java.time.ZonedDateTime;

import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.entity.mscv.VerificationExecution;
import eu.arrowhead.common.database.repository.mscv.ScriptRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationExecutionDetailRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationExecutionRepository;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionView;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.common.dto.shared.mscv.SshTargetDto;
import eu.arrowhead.common.dto.shared.mscv.VerificationRunResult;
import eu.arrowhead.core.mscv.MscvDefaults;
import eu.arrowhead.core.mscv.MscvDtoConverter;
import eu.arrowhead.core.mscv.Validation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static eu.arrowhead.core.mscv.Validation.LAYER_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.LIST_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.TARGET_NULL_ERROR_MESSAGE;

@Service
public class ExecutionService {

    private final Logger logger = LogManager.getLogger();
    private final VerificationService verificationService;
    private final VerificationExecutionDetailRepository executionDetailRepo;
    private final VerificationExecutionRepository executionRepo;
    private final TargetService targetService;
    private final ScriptRepository scriptRepo;
    private final MscvDefaults defaults;
    private final Validation validation;

    @Autowired
    public ExecutionService(final VerificationService verificationService,
                            final VerificationExecutionDetailRepository executionDetailRepo,
                            final VerificationExecutionRepository executionRepo,
                            final ScriptRepository scriptRepo,
                            final TargetService targetService,
                            final MscvDefaults defaults) {
        super();
        this.verificationService = verificationService;
        this.executionDetailRepo = executionDetailRepo;
        this.executionRepo = executionRepo;
        this.scriptRepo = scriptRepo;
        this.targetService = targetService;
        this.defaults = defaults;

        this.validation = new Validation();
    }

    @Transactional
    public VerificationExecutionView executeByIdAndTarget(final Long entryListId, final Long targetId) {
        logger.debug("executeByIdAndTarget({}, {}) started", entryListId, targetId);
        Assert.notNull(entryListId, LIST_NULL_ERROR_MESSAGE);
        Assert.notNull(targetId, TARGET_NULL_ERROR_MESSAGE);
        final VerificationEntryList entryList = verificationService.findListById(entryListId);

        final SshTarget target = targetService.getTargetById(targetId);

        return MscvDtoConverter.convert(execute(entryList, target));
    }

    @Transactional
    public VerificationExecutionView executeWithDefaultList(final SshTargetDto targetDto, final Layer layer) {
        logger.debug("executeWithDefaultList({}, {}) started", targetDto, layer);
        Assert.notNull(targetDto, TARGET_NULL_ERROR_MESSAGE);
        Assert.notNull(layer, LAYER_NULL_ERROR_MESSAGE);
        final SshTarget target = targetService.findOrCreate(targetDto.getName(), targetDto.getOs(), targetDto.getAddress(), targetDto.getPort());
        final VerificationEntryList suitableList = verificationService.findOrCreateSuitableList(target, layer);

        return MscvDtoConverter.convert(execute(suitableList, target));
    }

    private VerificationExecution execute(final VerificationEntryList entryList, final SshTarget target) {
        final var execution = new VerificationExecution(target, entryList, ZonedDateTime.now(), VerificationRunResult.IN_PROGRESS);
        // TODO get the script for each entry by mip, os, layer
        // TODO asynchronously execute the (physical) script
        // TODO record each result in mscv_verification_detail
        // TODO update mscv_verification_execution once all scripts finished
        return executionRepo.saveAndFlush(execution);
    }
}
