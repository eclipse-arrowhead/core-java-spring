package eu.arrowhead.core.mscv.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import eu.arrowhead.common.database.entity.mscv.Script;
import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.entity.mscv.VerificationEntry;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.entity.mscv.VerificationResult;
import eu.arrowhead.common.database.entity.mscv.VerificationResultDetail;
import eu.arrowhead.common.database.repository.mscv.VerificationExecutionDetailRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationExecutionRepository;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionView;
import eu.arrowhead.common.dto.shared.mscv.DetailSuccessIndicator;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.common.dto.shared.mscv.SuccessIndicator;
import eu.arrowhead.common.dto.shared.mscv.TargetDto;
import eu.arrowhead.core.mscv.MscvDtoConverter;
import eu.arrowhead.core.mscv.handlers.ExecutionHandler;
import eu.arrowhead.core.mscv.handlers.ExecutionHandlerFactory;
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
public class VerificationExecutionService {

    private final Logger logger = LogManager.getLogger();

    private final ExecutionHandlerFactory handlerFactory;
    private final VerificationService verificationService;
    private final VerificationExecutionDetailRepository executionDetailRepo;
    private final VerificationExecutionRepository executionRepo;
    private final TargetService targetService;
    private final ScriptService scriptService;

    @Autowired
    public VerificationExecutionService(final ExecutionHandlerFactory handlerFactory,
                                        final VerificationService verificationService,
                                        final VerificationExecutionDetailRepository executionDetailRepo,
                                        final VerificationExecutionRepository executionRepo,
                                        final ScriptService scriptService,
                                        final TargetService targetService) {
        super();
        this.handlerFactory = handlerFactory;
        this.verificationService = verificationService;
        this.executionDetailRepo = executionDetailRepo;
        this.executionRepo = executionRepo;
        this.scriptService = scriptService;
        this.targetService = targetService;
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
    public VerificationExecutionView executeWithDefaultList(final TargetDto targetDto, final Layer layer) {
        logger.debug("executeWithDefaultList({}, {}) started", targetDto, layer);
        Assert.notNull(targetDto, TARGET_NULL_ERROR_MESSAGE);
        Assert.notNull(layer, LAYER_NULL_ERROR_MESSAGE);
        final SshTarget target = targetService.findOrCreate(MscvDtoConverter.convert(targetDto));
        final VerificationEntryList suitableList = verificationService.findOrCreateSuitableList(target, layer);

        return MscvDtoConverter.convert(execute(suitableList, target));
    }

    private VerificationResult execute(final VerificationEntryList entryList, final SshTarget target) {

        final VerificationResult execution = executionRepo.saveAndFlush(
                new VerificationResult(target, entryList, ZonedDateTime.now(), SuccessIndicator.IN_PROGRESS));

        final Set<VerificationEntry> entries = entryList.getEntries();
        final List<VerificationResultDetail> detailList = new ArrayList<>();

        final Optional<ExecutionHandler<SshTarget>> optionalHandler = handlerFactory.find(target);
        if (optionalHandler.isPresent()) {

            final var executionHandler = optionalHandler.get();
            try {

                // prepare result for each verification entry. result will be adapted during execution run.
                for (VerificationEntry entry : entries) {
                    final Optional<Script> optionalScript = scriptService.findScriptFor(entry.getMip(), target.getOs(), entryList.getLayer());

                    if (optionalScript.isPresent()) {
                        final var detail = new VerificationResultDetail(execution, entry, optionalScript.get(),
                                                                        DetailSuccessIndicator.NOT_APPLICABLE, null);
                        detailList.add(detail);
                    } else {
                        detailList.add(createNotApplicableDetail(execution, entry));
                    }
                }

                executionHandler.performVerification(execution, detailList);

            } catch (final MscvException e) {
                execution.setResult(SuccessIndicator.ERROR);
                logger.error("Verification execution failed: {}", e.getMessage());
            }
        } else {
            execution.setResult(SuccessIndicator.SKIPPED);
            logger.warn("No ExecutionHandler available for target: {}", target);
        }

        executionDetailRepo.saveAll(detailList);
        return executionRepo.saveAndFlush(execution);
    }

    private VerificationResultDetail createNotApplicableDetail(final VerificationResult result, final VerificationEntry entry) {
        return new VerificationResultDetail(result, entry, null, DetailSuccessIndicator.NOT_APPLICABLE, null);
    }
}
