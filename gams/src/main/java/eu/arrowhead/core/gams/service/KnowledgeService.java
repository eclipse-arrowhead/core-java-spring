package eu.arrowhead.core.gams.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import eu.arrowhead.common.database.entity.AbstractEvaluation;
import eu.arrowhead.common.database.entity.AbstractPolicy;
import eu.arrowhead.common.database.entity.AbstractSensorData;
import eu.arrowhead.common.database.entity.Aggregation;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.Knowledge;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.repository.AggregationRepository;
import eu.arrowhead.common.database.repository.AnalysisRepository;
import eu.arrowhead.common.database.repository.KnowledgeRepository;
import eu.arrowhead.common.database.repository.PolicyRepository;
import eu.arrowhead.core.gams.DataValidation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class KnowledgeService {

    private final Logger logger = LogManager.getLogger();
    private final DataValidation validation = new DataValidation();

    private final AggregationRepository aggregationRepository;
    private final AnalysisRepository analysisRepository;
    private final PolicyRepository policyRepository;
    private final SensorService sensorService;
    private final KnowledgeRepository knowledgeRepository;

    @Autowired
    public KnowledgeService(final AggregationRepository aggregationRepository,
                            final AnalysisRepository analysisRepository,
                            final PolicyRepository policyRepository,
                            final SensorService sensorService, final KnowledgeRepository knowledgeRepository) {
        this.aggregationRepository = aggregationRepository;
        this.analysisRepository = analysisRepository;
        this.policyRepository = policyRepository;
        this.sensorService = sensorService;
        this.knowledgeRepository = knowledgeRepository;
    }

    public Optional<Knowledge> get(final GamsInstance instance, final String key) {
        validation.verify(instance);
        Assert.hasText(key, "Knowledge key must not be empty");
        return knowledgeRepository.findByInstanceAndKey(instance, key);
    }

    @Transactional
    public void put(final GamsInstance instance, final String key, final String value) {
        validation.verify(instance);
        Assert.hasText(key, "Knowledge key must not be empty");
        Assert.hasText(value, "Knowledge value must not be empty");

        final Optional<Knowledge> optionalKnowledge = get(instance, key);
        final Knowledge knowledge;

        try {
            if (optionalKnowledge.isPresent()) {
                knowledge = optionalKnowledge.get();
                knowledge.setValue(value);
            } else {
                knowledge = new Knowledge(instance, key, value);
            }
            knowledgeRepository.saveAndFlush(knowledge);
        } catch(final Exception e) {
            logger.error("Unable to store Knowledge {}={}: {}", key, value, e.getMessage());
        }
    }

    public List<Aggregation> loadPreAnalysis(final Sensor sensor) {
        validation.verify(sensor);
        return aggregationRepository.findBySensor(sensor);
    }

    public List<AbstractEvaluation> loadEvaluation(final Sensor sensor) {
        validation.verify(sensor);
        return analysisRepository.findBySensor(sensor);
    }

    public List<AbstractPolicy> loadPolicy(final Sensor sensor) {
        validation.verify(sensor);
        return policyRepository.findBySensor(sensor);
    }

    @Transactional
    public void storeSensorData(final AbstractSensorData<?> data) {
        sensorService.store(data);
    }

    @Transactional
    public AbstractSensorData<?> storeSensorData(final Sensor eventSensor, final String value) {
        return sensorService.store(eventSensor, ZonedDateTime.now(), value, eventSensor.getAddress());
    }
}
