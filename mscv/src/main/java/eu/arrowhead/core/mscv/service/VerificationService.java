package eu.arrowhead.core.mscv.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import eu.arrowhead.common.database.entity.mscv.Script;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.entity.mscv.VerificationEntry;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.repository.mscv.VerificationEntryListRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationEntryRepository;
import eu.arrowhead.common.database.view.mscv.VerificationListView;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.core.mscv.MscvDefaults;
import eu.arrowhead.core.mscv.MscvDtoConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;
import static eu.arrowhead.core.mscv.Validation.ID_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.LAYER_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.NAME_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.TARGET_NULL_ERROR_MESSAGE;

@Service
public class VerificationService {

    private static final String DEFAULT_LIST_DESCRIPTION = "An automatically created default list";

    private final Logger logger = LogManager.getLogger();
    private VerificationEntryListRepository verListRepository;
    private VerificationEntryRepository verEntryRepository;
    private ScriptService scriptService;
    private MscvDefaults defaults;

    @Autowired
    public VerificationService(final VerificationEntryListRepository verListRepository,
                               final VerificationEntryRepository verEntryRepository,
                               final ScriptService scriptService,
                               final MscvDefaults defaults) {
        super();
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
        logger.debug("getListByName({}) started", name);
        Assert.notNull(name, NAME_NULL_ERROR_MESSAGE);
        final Optional<VerificationEntryList> optional = verListRepository.findOneByNameAndLayer(name, layer);
        return MscvDtoConverter.convertToView(optional.orElseThrow(notFoundException("Verification list name")));
    }

    @Transactional(readOnly = true)
    protected VerificationEntryList findListById(final Long id) {
        logger.debug("findListById({}) started", id);
        Assert.notNull(id, ID_NULL_ERROR_MESSAGE);
        final Optional<VerificationEntryList> optional = verListRepository.findById(id);
        return optional.orElseThrow(notFoundException("Verification list ID"));
    }

    @Transactional
    protected VerificationEntryList findOrCreateSuitableList(final Target target, final Layer layer) {
        logger.debug("findSuitableList({}) started", target);
        Assert.notNull(target, TARGET_NULL_ERROR_MESSAGE);
        Assert.notNull(layer, LAYER_NULL_ERROR_MESSAGE);

        // no strategy exists yet. using default list
        Optional<VerificationEntryList> defaultList = verListRepository.findOneByNameAndLayer(defaults.getListName(), layer);
        //return defaultList.orElseThrow(() -> new MscvException("No suitable list found"));
        return defaultList.orElseGet(() -> createDefaultList(layer));
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
}
