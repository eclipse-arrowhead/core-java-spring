package eu.arrowhead.core.mscv.database.service;

import eu.arrowhead.core.mscv.MscvDtoConverter;
import eu.arrowhead.common.dto.shared.mscv.VerificationRunResult;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.entity.mscv.VerificationExecution;
import eu.arrowhead.common.database.repository.mscv.ScriptRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationEntryListRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationExecutionDetailRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationExecutionRepository;
import eu.arrowhead.common.database.view.mscv.TargetView;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionView;
import eu.arrowhead.common.database.view.mscv.VerificationListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class MscvExecutionService {

    private static final String NULL_ERROR = " must not be null";
    private static final String NOT_FOUND_ERROR = " not found";

    private final Logger logger = LogManager.getLogger();
    private final VerificationEntryListRepository verificationListRepo;
    private final ScriptRepository scriptRepo;
    private final VerificationExecutionRepository executionRepo;
    private final VerificationExecutionDetailRepository executionDetailRepos;
    private final MscvTargetService targetService;

    @Autowired
    public MscvExecutionService(final VerificationEntryListRepository verificationListRepo,
                                final ScriptRepository scriptRepo,
                                final VerificationExecutionRepository executionRepo,
                                final VerificationExecutionDetailRepository executionDetailRepos,
                                final MscvTargetService targetService) {
        super();
        this.verificationListRepo = verificationListRepo;
        this.scriptRepo = scriptRepo;
        this.executionRepo = executionRepo;
        this.executionDetailRepos = executionDetailRepos;
        this.targetService = targetService;
    }

    @Transactional(rollbackOn = Exception.class)
    public VerificationExecutionView executeByIdAndTarget(final Long id, final TargetView targetView) {
        Assert.notNull(id, "id" + NULL_ERROR);
        Assert.notNull(targetView, "targetView" + NULL_ERROR);
        final Optional<VerificationEntryList> optional = verificationListRepo.findById(id);
        final VerificationEntryList entryList = optional.orElseThrow(notFoundException("id"));

        final Target target = targetService.findOrCreateTarget(targetView);

        // TODO actually start the execution
        final var execution = new VerificationExecution(target, entryList, ZonedDateTime.now(), VerificationRunResult.IN_PROGRESS);

        return MscvDtoConverter.convert(executionRepo.saveAndFlush(execution));
    }

    @Transactional(rollbackOn = Exception.class)
    public VerificationListView getListById(final Long id) {
        Assert.notNull(id, "id" + NULL_ERROR);
        final Optional<VerificationEntryList> optional = verificationListRepo.findById(id);
        return MscvDtoConverter.convert(optional.orElseThrow(notFoundException("id")));
    }

    @Transactional(rollbackOn = Exception.class)
    public VerificationListView getListByName(final String name) {
        Assert.notNull(name, "name" + NULL_ERROR);
        final Optional<VerificationEntryList> optional = verificationListRepo.findOneByName(name);
        return MscvDtoConverter.convert(optional.orElseThrow(notFoundException("name")));
    }

    private Supplier<IllegalArgumentException> notFoundException(final String variable) {
        return () -> new IllegalArgumentException(variable + NOT_FOUND_ERROR);
    }

}
