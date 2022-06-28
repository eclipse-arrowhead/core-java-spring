package eu.arrowhead.core.mscv.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import javax.persistence.PersistenceException;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreSystemRegistrationProperties;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.database.entity.mscv.Mip;
import eu.arrowhead.common.database.entity.mscv.Script;
import eu.arrowhead.common.database.repository.mscv.MipRepository;
import eu.arrowhead.common.database.repository.mscv.ScriptRepository;
import eu.arrowhead.common.database.view.mscv.MipView;
import eu.arrowhead.common.database.view.mscv.MipViewImpl;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.common.dto.shared.mscv.OS;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.mscv.MscvDefaults;
import eu.arrowhead.core.mscv.Validation;
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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static eu.arrowhead.core.mscv.Constants.PARAMETER_LAYER;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_IDENTIFIER;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_OS;
import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;
import static eu.arrowhead.core.mscv.Validation.CATEGORY_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.EXAMPLE_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.ID_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.LAYER_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.MIP_IDENTIFIER_FORMAT_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.MIP_IDENTIFIER_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.MIP_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.NAME_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.OS_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.PAGE_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.SCRIPT_CONTENT_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.SCRIPT_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.controller.ScriptMgmtController.QUALIFY_SCRIPT_URI;

@Service
public class ScriptService {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(ScriptService.class);

    private final MscvDefaults defaults;
    private final ScriptRepository scriptRepository;
    private final MipRepository mipRepository;
    private final UriComponents uriTemplate;


    @Autowired
    public ScriptService(final MscvDefaults defaults, final ScriptRepository scriptRepository,
                         final MipRepository mipRepository, final SSLProperties sslProperties,
                         final CoreSystemRegistrationProperties properties) {
        this.defaults = defaults;
        this.scriptRepository = scriptRepository;
        this.mipRepository = mipRepository;

        this.uriTemplate = UriComponentsBuilder.newInstance()
                                               .scheme(sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP)
                                               .host(properties.getCoreSystemDomainName())
                                               .port(properties.getCoreSystemDomainPort())
                                               .pathSegment(CommonConstants.MSCV_URI.substring(1), QUALIFY_SCRIPT_URI.substring(1))
                                               .build();
    }

    @Transactional(readOnly = true)
    public Optional<Script> findScriptFor(final Mip mip, final Layer layer, final OS os) {
        try {
            logger.debug("findScriptFor({},{},{}) started", mip, layer, os);
            Assert.notNull(mip, MIP_NULL_ERROR_MESSAGE);
            Assert.notNull(os, OS_NULL_ERROR_MESSAGE);
            Assert.notNull(layer, LAYER_NULL_ERROR_MESSAGE);
            return scriptRepository.findOneByMipAndLayerAndOs(mip, layer, os);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to find Scripts", pe);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Script> findScriptFor(final String identifier, final Layer layer, final OS os) {
        try {
            logger.debug("findScriptFor({},{},{}) started", identifier, layer, os);
            Assert.hasText(identifier, MIP_IDENTIFIER_NULL_ERROR_MESSAGE);
            Assert.notNull(layer, LAYER_NULL_ERROR_MESSAGE);
            Assert.notNull(os, OS_NULL_ERROR_MESSAGE);

            final Matcher matcher = Validation.MIP_IDENTIFIER_PATTERN.matcher(identifier);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(MIP_IDENTIFIER_FORMAT_ERROR_MESSAGE);
            }

            final String categoryAbbreviation = matcher.group(1).trim();
            final Integer externalId = Integer.valueOf(matcher.group(2));
            final Optional<Mip> optionalMip = mipRepository.findByCategoryAbbreviationAndExtId(categoryAbbreviation, externalId);
            final Mip mip = optionalMip.orElseThrow(notFoundException("MIP"));

            return scriptRepository.findOneByMipAndLayerAndOs(mip, layer, os);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to find Scripts", pe);
        }
    }

    @Transactional
    public Script create(final ByteArrayOutputStream content, final String catAbbr, final Integer extId,
                         final Layer layer, final OS os) {
        try {
            logger.debug("create(<stream>,{},{},{},{}) started", catAbbr, extId, layer, os);
            Assert.notNull(content, SCRIPT_CONTENT_NULL_ERROR_MESSAGE);
            Assert.isTrue(content.size() > 0, SCRIPT_CONTENT_NULL_ERROR_MESSAGE);
            Assert.hasText(catAbbr, CATEGORY_NULL_ERROR_MESSAGE);
            Assert.notNull(extId, ID_NULL_ERROR_MESSAGE);
            Assert.notNull(layer, LAYER_NULL_ERROR_MESSAGE);
            Assert.notNull(os, OS_NULL_ERROR_MESSAGE);

            final Optional<Mip> optionalMip = mipRepository.findByCategoryAbbreviationAndExtId(catAbbr, extId);
            final Mip mip = optionalMip.orElseThrow(notFoundException("MIP"));
            final Script script = new Script(mip, layer, os, null);

            if (exists(script)) { throw new InvalidParameterException("Script meta information exist already"); }

            safeContent(script, content);
            return scriptRepository.saveAndFlush(script);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to create Scripts", pe);
        }
    }

    @Transactional(readOnly = true)
    public boolean exists(final Script script) {
        try {
            logger.debug("exists({}) started", script);
            Assert.notNull(script, SCRIPT_NULL_ERROR_MESSAGE);
            return scriptRepository.exists(Example.of(script, ExampleMatcher.matchingAll()));
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to verify existence of Scripts", pe);
        }
    }

    @Transactional(readOnly = true)
    public Page<Script> pageAll(final Pageable pageable) {
        try {
            logger.debug("pageAll({}) started", pageable);
            Assert.notNull(pageable, PAGE_NULL_ERROR_MESSAGE);
            return scriptRepository.findAll(pageable);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to query Scripts", pe);
        }
    }

    @Transactional(readOnly = true)
    public Page<Script> pageByExample(final Example<Script> example, final Pageable pageable) {
        try {
            logger.debug("pageByExample({},{}) started", example, pageable);
            Assert.notNull(example, EXAMPLE_NULL_ERROR_MESSAGE);
            Assert.notNull(pageable, PAGE_NULL_ERROR_MESSAGE);
            return scriptRepository.findAll(example, pageable);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to find Scripts", pe);
        }
    }

    @Transactional
    public Script replace(final Script oldScript, final Script newScript) {
        try {
            logger.debug("replace({},{}) started", oldScript, newScript);
            Assert.notNull(oldScript, "old " + SCRIPT_NULL_ERROR_MESSAGE);
            Assert.notNull(newScript, "new " + SCRIPT_NULL_ERROR_MESSAGE);

            verifyScriptPath(newScript);
            oldScript.setLayer(newScript.getLayer());
            oldScript.setMip(newScript.getMip());
            oldScript.setOs(newScript.getOs());
            return scriptRepository.saveAndFlush(oldScript);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to replace Scripts", pe);
        }
    }

    @Transactional
    public Script replace(final Script script, final ByteArrayOutputStream content) {
        try {
            logger.debug("replace({},<stream>) started", script);
            Assert.notNull(script, SCRIPT_NULL_ERROR_MESSAGE);
            safeContent(script, content);
            return script;
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to replace Scripts", pe);
        }
    }

    @Transactional
    public void delete(final String name, final Layer layer, final OS os) {
        try {
            logger.debug("delete({}) started", name);
            Assert.hasText(name, NAME_NULL_ERROR_MESSAGE);

            final Optional<Script> optionalScript = findScriptFor(name, layer, os);
            optionalScript.ifPresent(scriptRepository::delete);
            scriptRepository.flush();
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to delete Scripts", pe);
        }
    }

    public String createUriPath(final Script script) {
        final MipView mipView = new MipViewImpl(script.getMip());
        final Map<String, String> vars = Map.of(PARAMETER_MIP_IDENTIFIER, mipView.getIdentifier(),
                                                PARAMETER_LAYER, script.getLayer().path(),
                                                PARAMETER_OS, script.getOs().path());
        return uriTemplate.expand(vars).toUriString();
    }

    protected Set<Script> findAllByLayer(final Layer layer) {
        try {
            logger.debug("findAllByLayer({}) started", layer);
            Assert.notNull(layer, LAYER_NULL_ERROR_MESSAGE);
            return scriptRepository.findAllByLayer(layer);
        } catch (final PersistenceException pe) {
            throw new ArrowheadException("Unable to find Scripts", pe);
        }
    }

    private void safeContent(final Script script, final ByteArrayOutputStream content) {
        final String path = createPhysicalPath(script);
        final File file = new File(path);
        try {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            try (final FileOutputStream out = new FileOutputStream(file)) {
                content.writeTo(out);
                script.setPhysicalPath(path);
            }
        } catch (final IOException e) {
            throw new ArrowheadException("Unknown IO Exception", e);
        }
    }

    private void verifyScriptPath(final Script script) {
        final File file = new File(script.getPhysicalPath());
        if (!file.exists()) {
            logger.warn("No script content found for {}", script);
            throw new IllegalArgumentException("Script location not valid.");
        }
    }

    private String createPhysicalPath(final Script script) {
        final MipView mipView = new MipViewImpl(script.getMip());
        final Path path = Path.of(defaults.getDefaultPath())
                              .resolve(mipView.getStandard())
                              .resolve(script.getOs().path())
                              .resolve(script.getLayer().path())
                              .resolve(mipView.getIdentifier() + ".sh")
                              .toAbsolutePath();
        return path.toString().replaceAll(" ", "_");
    }
}
