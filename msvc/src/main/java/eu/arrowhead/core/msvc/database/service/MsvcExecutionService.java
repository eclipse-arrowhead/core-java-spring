package eu.arrowhead.core.msvc.database.service;

import eu.arrowhead.core.msvc.MsvcDtoConverter;
import eu.arrowhead.core.msvc.database.VerificationRunResult;
import eu.arrowhead.core.msvc.database.entities.Target;
import eu.arrowhead.core.msvc.database.entities.VerificationEntryList;
import eu.arrowhead.core.msvc.database.entities.VerificationExecution;
import eu.arrowhead.core.msvc.database.repositories.ScriptRepository;
import eu.arrowhead.core.msvc.database.repositories.VerificationEntryListRepository;
import eu.arrowhead.core.msvc.database.repositories.VerificationExecutionDetailRepository;
import eu.arrowhead.core.msvc.database.repositories.VerificationExecutionRepository;
import eu.arrowhead.core.msvc.database.view.TargetView;
import eu.arrowhead.core.msvc.database.view.VerificationExecutionView;
import eu.arrowhead.core.msvc.database.view.VerificationListView;
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
public class MsvcExecutionService {

    private static final String NULL_ERROR = " must not be null";
    private static final String NOT_FOUND_ERROR = " not found";

    private final Logger logger = LogManager.getLogger();
    private final VerificationEntryListRepository verificationListRepo;
    private final ScriptRepository scriptRepo;
    private final VerificationExecutionRepository executionRepo;
    private final VerificationExecutionDetailRepository executionDetailRepos;
    private final MsvcTargetService targetService;

    @Autowired
    public MsvcExecutionService(final VerificationEntryListRepository verificationListRepo,
                                final ScriptRepository scriptRepo,
                                final VerificationExecutionRepository executionRepo,
                                final VerificationExecutionDetailRepository executionDetailRepos,
                                final MsvcTargetService targetService) {
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

        return MsvcDtoConverter.convert(executionRepo.saveAndFlush(execution));
    }

    @Transactional(rollbackOn = Exception.class)
    public VerificationListView getListById(final Long id) {
        Assert.notNull(id, "id" + NULL_ERROR);
        final Optional<VerificationEntryList> optional = verificationListRepo.findById(id);
        return MsvcDtoConverter.convert(optional.orElseThrow(notFoundException("id")));
    }

    @Transactional(rollbackOn = Exception.class)
    public VerificationListView getListByName(final String name) {
        Assert.notNull(name, "name" + NULL_ERROR);
        final Optional<VerificationEntryList> optional = verificationListRepo.findOneByName(name);
        return MsvcDtoConverter.convert(optional.orElseThrow(notFoundException("name")));
    }

    private Supplier<IllegalArgumentException> notFoundException(final String variable) {
        return () -> new IllegalArgumentException(variable + NOT_FOUND_ERROR);
    }

}
