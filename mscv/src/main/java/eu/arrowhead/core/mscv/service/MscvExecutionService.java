package eu.arrowhead.core.mscv.service;

import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.entity.mscv.VerificationExecution;
import eu.arrowhead.common.database.repository.mscv.ScriptRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationEntryListRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationExecutionDetailRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationExecutionRepository;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionView;
import eu.arrowhead.common.database.view.mscv.VerificationListView;
import eu.arrowhead.common.dto.shared.mscv.VerificationRunResult;
import eu.arrowhead.core.mscv.MscvDefaults;
import eu.arrowhead.core.mscv.MscvDtoConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Optional;

import static eu.arrowhead.core.mscv.MscvUtilities.ID_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.LIST_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.NAME_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.TARGET_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;

@Service
public class MscvExecutionService {

    private final Logger logger = LogManager.getLogger();
    private final MscvDefaults defaults;
    private final VerificationExecutionDetailRepository executionDetailRepos;
    private final VerificationEntryListRepository verificationListRepo;
    private final VerificationExecutionRepository executionRepo;
    private final MscvTargetService targetService;
    private final ScriptRepository scriptRepo;

    @Autowired
    public MscvExecutionService(final MscvDefaults defaults,
                                final VerificationEntryListRepository verificationListRepo,
                                final ScriptRepository scriptRepo,
                                final VerificationExecutionRepository executionRepo,
                                final VerificationExecutionDetailRepository executionDetailRepos,
                                final MscvTargetService targetService) {
        super();
        this.defaults = defaults;
        this.verificationListRepo = verificationListRepo;
        this.scriptRepo = scriptRepo;
        this.executionRepo = executionRepo;
        this.executionDetailRepos = executionDetailRepos;
        this.targetService = targetService;
    }

    @Transactional(rollbackOn = Exception.class)
    public VerificationExecutionView executeByIdAndTarget(final Long entryListId, final Long targetId) {
        logger.debug("executeByIdAndTarget({}, {}) started", entryListId, targetId);
        Assert.notNull(entryListId, LIST_NOT_NULL);
        Assert.notNull(targetId, TARGET_NOT_NULL);
        final Optional<VerificationEntryList> optionalList = verificationListRepo.findById(entryListId);
        final VerificationEntryList entryList = optionalList.orElseThrow(notFoundException("Verification list"));

        final SshTarget target = targetService.getTargetById(targetId);

        // TODO actually start the execution
        final var execution = new VerificationExecution(target, entryList, ZonedDateTime.now(), VerificationRunResult.IN_PROGRESS);
        return MscvDtoConverter.convert(executionRepo.saveAndFlush(execution));
    }

    @Transactional(rollbackOn = Exception.class)
    public VerificationListView getListById(final Long id) {
        logger.debug("getListById({}) started", id);
        Assert.notNull(id, ID_NOT_NULL);
        final Optional<VerificationEntryList> optional = verificationListRepo.findById(id);
        return MscvDtoConverter.convert(optional.orElseThrow(notFoundException("id")));
    }

    @Transactional(rollbackOn = Exception.class)
    public VerificationListView getListByName(final String name) {
        logger.debug("getListByName({}) started", name);
        Assert.notNull(name, NAME_NOT_NULL);
        final Optional<VerificationEntryList> optional = verificationListRepo.findOneByName(name);
        return MscvDtoConverter.convert(optional.orElseThrow(notFoundException("name")));
    }

    public VerificationEntryList findSuitableList(final Target target) throws MscvException {
        logger.debug("findSuitableList({}) started", target);

        // no strategy exists yet. using default list
        Optional<VerificationEntryList> defaultList = verificationListRepo.findOneByName(defaults.getDefaultList());
        return defaultList.orElseThrow(() -> new MscvException("No suitable list found"));
    }
}
