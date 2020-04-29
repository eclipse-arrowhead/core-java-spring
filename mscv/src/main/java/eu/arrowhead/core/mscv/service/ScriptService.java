package eu.arrowhead.core.mscv.service;

import java.util.Set;

import eu.arrowhead.common.database.entity.mscv.Script;
import eu.arrowhead.common.database.repository.mscv.MipRepository;
import eu.arrowhead.common.database.repository.mscv.ScriptRepository;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.core.mscv.MscvDefaults;
import eu.arrowhead.core.mscv.Validation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static eu.arrowhead.core.mscv.Validation.LAYER_NULL_ERROR_MESSAGE;

@Service
public class ScriptService {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(ScriptService.class);

    private final MscvDefaults defaults;
    private final ScriptRepository scriptRepository;
    private final MipRepository mipRepository;

    private final Validation validation;

    @Autowired
    public ScriptService(final MscvDefaults defaults, final ScriptRepository scriptRepository,
                         final MipRepository mipRepository) {
        this.defaults = defaults;
        this.scriptRepository = scriptRepository;
        this.mipRepository = mipRepository;

        this.validation = new Validation();
    }


    //=================================================================================================
    // methods

    @Transactional(readOnly = true)
    protected Set<Script> findAllByLayer(final Layer layer) {
        logger.debug("findAllByLayer({}) started", layer);
        Assert.notNull(layer, LAYER_NULL_ERROR_MESSAGE);
        return scriptRepository.findAllByLayer(layer);
    }

}
