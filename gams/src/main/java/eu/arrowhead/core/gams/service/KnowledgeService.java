package eu.arrowhead.core.gams.service;

import java.util.List;
import java.util.Optional;

import eu.arrowhead.common.database.entity.AbstractAnalysis;
import eu.arrowhead.common.database.entity.Aggregation;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.Knowledge;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.repository.AggregationRepository;
import eu.arrowhead.common.database.repository.AnalysisRepository;
import eu.arrowhead.common.database.repository.KnowledgeRepository;
import eu.arrowhead.core.gams.DataValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class KnowledgeService {

    private final DataValidation validation = new DataValidation();

    private final AggregationRepository aggregationRepository;
    private final AnalysisRepository analysisRepository;

    private final KnowledgeRepository knowledgeRepository;

    @Autowired
    public KnowledgeService(final AggregationRepository aggregationRepository,
                            final AnalysisRepository analysisRepository,
                            final KnowledgeRepository knowledgeRepository) {
        this.aggregationRepository = aggregationRepository;
        this.analysisRepository = analysisRepository;
        this.knowledgeRepository = knowledgeRepository;
    }

    public Optional<Knowledge> get(final GamsInstance instance, final String key) {
        validation.verify(instance);
        Assert.hasText(key, "Knowledge key must not be empty");
        return knowledgeRepository.findByInstanceAndKey(instance, key);
    }

    public void put(final GamsInstance instance, final String key, final String value) {
        validation.verify(instance);
        Assert.hasText(key, "Knowledge key must not be empty");
        Assert.hasText(value, "Knowledge value must not be empty");
        final Knowledge knowledge = new Knowledge(instance, key, value);
        knowledgeRepository.saveAndFlush(knowledge);
    }

    public List<Aggregation> loadPreAnalysis(final Sensor sensor) {
        validation.verify(sensor);
        return aggregationRepository.findBySensor(sensor);
    }

    public List<AbstractAnalysis> loadAnalysis(final Sensor sensor) {
        validation.verify(sensor);
        return analysisRepository.findBySensor(sensor);
    }
}
