package eu.arrowhead.core.gams.service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;

import eu.arrowhead.common.database.entity.AbstractEvaluation;
import eu.arrowhead.common.database.entity.AbstractPolicy;
import eu.arrowhead.common.database.entity.AbstractSensorData;
import eu.arrowhead.common.database.entity.Aggregation;
import eu.arrowhead.common.database.entity.ApiCallPolicy;
import eu.arrowhead.common.database.entity.CountingAggregation;
import eu.arrowhead.common.database.entity.CountingEvaluation;
import eu.arrowhead.common.database.entity.DoubleSensorData;
import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.Knowledge;
import eu.arrowhead.common.database.entity.LongSensorData;
import eu.arrowhead.common.database.entity.MatchPolicy;
import eu.arrowhead.common.database.entity.ProcessableAction;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.entity.SetPointEvaluation;
import eu.arrowhead.common.database.entity.TransformPolicy;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.gams.DataValidation;
import eu.arrowhead.core.gams.controller.SetPointController;
import eu.arrowhead.core.gams.dto.AbstractActionWrapper;
import eu.arrowhead.core.gams.dto.GamsPhase;
import eu.arrowhead.core.gams.dto.ProcessingState;
import eu.arrowhead.core.gams.rest.dto.SensorType;
import eu.arrowhead.core.gams.utility.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import parser.MathExpression;

@Service
public class MapeKService {

    private final Logger logger = LogManager.getLogger();

    private final DataValidation validation = new DataValidation();

    private final EventService eventService;
    private final SensorService sensorService;
    private final KnowledgeService knowledgeService;
    private final TimeoutService timeoutService;
    private final ActionAssemblyService apiCallService;
    private final ExecutorService executorService;


    @Autowired
    public MapeKService(final EventService eventService,
                        final SensorService sensorService,
                        final KnowledgeService knowledgeService,
                        final TimeoutService timeoutService,
                        final ActionAssemblyService apiCallService,
                        final ExecutorService executorService) {
        this.eventService = eventService;
        this.sensorService = sensorService;
        this.knowledgeService = knowledgeService;
        this.timeoutService = timeoutService;
        this.apiCallService = apiCallService;
        this.executorService = executorService;
    }

    @Transactional
    public void publish(final Sensor sensor, final ZonedDateTime timestamp, final Object data, final String address) {
        validation.verify(sensor);

        try {
            final AbstractSensorData sensorData = sensorService.store(sensor, timestamp, data, address);
            logger.debug(GamsPhase.MONITOR.getMarker(), "Stored new sensor data: {}", sensorData);
            eventService.createMonitorEventWithDelay(sensor, sensorData);
        } catch (Exception e) {
            throw new ArrowheadException(e.getMessage());
        }
    }

    @Transactional
    public void monitor(final Event source) {
        validation.verify(source);
        logger.debug(source.getMarker(), "Received monitor event: {}", source::shortToString);

        final Sensor sourceSensor = source.getSensor();
        final List<Aggregation> preAnalysis = knowledgeService.loadPreAnalysis(sourceSensor);

        if (sourceSensor.getType() == SensorType.EVENT) {
            noAggregation(sourceSensor, source);
        } else if (preAnalysis.isEmpty()) {
            noAggregation(sourceSensor, source);
        } else {
            processAggregation(preAnalysis, sourceSensor, source);
        }
    }

    @Transactional
    public void analyze(final Event source) {
        validation.verify(source);
        logger.info(source.getMarker(), "Received analyze event: {}", source::shortToString);

        timeoutService.rescheduleTimeoutEvent(source);
        final Sensor sensor = source.getSensor();

        final List<AbstractEvaluation> evaluations = knowledgeService.loadEvaluation(source.getSensor());
        if(evaluations.isEmpty()) {
            logger.warn("No Evaluations available for {}", source.getSensor());
            return;
        }

        for (AbstractEvaluation evaluation : evaluations) {

            final AbstractSensorData sensorData;
            final Sensor eventSensor = sensorService.getEventSensor(source.getSensor().getInstance(), evaluation.getUidString());
            logger.debug("Processing evaluation: {}", evaluation::shortToString);

            switch (evaluation.getType()) {
                case SET_POINT:
                    final double setPoint = calculateSetPoint(sensor, (SetPointEvaluation) evaluation, source.getData());
                    knowledgeService.put(sensor.getInstance(), evaluation.getTargetKnowledge(), String.valueOf(setPoint));

                    sensorData = sensorService.store(eventSensor, ZonedDateTime.now(), setPoint, eventSensor.getAddress());
                    eventService.createPlanEvent(eventSensor, sensorData);

                    break;
                case COUNTING:
                    final CountingEvaluation countingEvaluation = (CountingEvaluation) evaluation;
                    final ZonedDateTime validFrom = ZonedDateTime.now().minus(countingEvaluation.getTimeValue(), countingEvaluation.getTimeUnit());
                    final long count = sensorService.count(sensor, validFrom);

                    if (count >= countingEvaluation.getCount()) {
                        knowledgeService.put(sensor.getInstance(), evaluation.getTargetKnowledge(), String.valueOf(count));
                        sensorData = sensorService.store(eventSensor, ZonedDateTime.now(), count, eventSensor.getAddress());
                        eventService.createPlanEvent(eventSensor, sensorData);
                    }
                    break;
                default: throw new IllegalStateException("Unexpected value: " + evaluation.getType());
            }
        }
    }

    @Transactional
    public void plan(final Event source) {
        validation.verify(source);
        logger.info(source.getMarker(), "Received plan event: {}", source::shortToString);

        final List<AbstractPolicy> policies = knowledgeService.loadPolicy(source.getSensor());
        if(policies.isEmpty()) {
            logger.warn("No Policies available for {}", source.getSensor());
            return;
        }

        for (AbstractPolicy policy : policies) {
            final AbstractSensorData sensorData;
            final Sensor eventSensor = sensorService.getEventSensor(source.getSensor().getInstance(), policy.getUidString());
            logger.debug("Processing policy: {}", policy::shortToString);

            switch (policy.getType()) {
                case MATCH:
                    final MatchPolicy matchPolicy = (MatchPolicy) policy;
                    logger.info("Performing MATCH: {}", matchPolicy::shortToString);

                    final Optional<Knowledge> optionalKnowledge = knowledgeService.get(eventSensor.getInstance(), policy.getSourceKnowledge());
                    if (optionalKnowledge.isPresent()) {
                        final Knowledge knowledge = optionalKnowledge.get();
                        if(evaluateMatchPolicy(matchPolicy, knowledge)) {
                            sensorData = sensorService.store(eventSensor, ZonedDateTime.now(), policy.getTargetKnowledge(), eventSensor.getAddress());
                            eventService.createExecuteEvent(eventSensor, sensorData);
                        }
                    } else {
                        if (evaluateMatchPolicy(matchPolicy, source.getData())) {
                            sensorData = sensorService.store(eventSensor, ZonedDateTime.now(), policy.getTargetKnowledge(), eventSensor.getAddress());
                            eventService.createExecuteEvent(eventSensor, sensorData);
                        }
                    }
                    break;
                case API_CALL:
                    final ProcessableAction action = ((ApiCallPolicy) policy).getApiCall();
                    logger.info("Performing API CALL: {}", action::shortToString);
                    final AbstractActionWrapper wrapper = apiCallService.assembleRunnable(source, action);
                    wrapper.runWithResult(eventSensor);
                    break;
                case TRANSFORM:
                    final TransformPolicy transformPolicy = (TransformPolicy) policy;
                    logger.info("Performing TRANSFORM: {}", policy::shortToString);
                    final MathExpression expression = new MathExpression(transformPolicy.getExpression());
                    expression.setValue(transformPolicy.getVariable(), source.getData());

                    final String solution = expression.solve();
                    sensorData = sensorService.store(eventSensor, ZonedDateTime.now(), solution, eventSensor.getAddress());
                    eventService.createExecuteEvent(eventSensor, sensorData);
                    break;
                case NONE:
                    logger.info("Performing Nothing: {}", policy::shortToString);
                    sensorData = sensorService.store(eventSensor, ZonedDateTime.now(), source.getData(), eventSensor.getAddress());
                    eventService.createExecuteEvent(eventSensor, sensorData);
                    break;
                default: throw new IllegalStateException("Unexpected value: " + policy.getType());
            }
        }
    }

    @Transactional
    public void execute(final Event event) {
        validation.verify(event);
        logger.info(event.getMarker(), "Received execute event: {}", event::shortToString);
        final Sensor sourceSensor = event.getSensor();
        final GamsInstance instance = sourceSensor.getInstance();
        final Runnable actionPlan = apiCallService.assembleActionPlan(instance, event);
        executorService.submit(actionPlan);
    }

    public void failure(final Event event) {
        logger.fatal(GamsPhase.FAILURE.getMarker(), event);
    }

    private void processAggregation(final List<Aggregation> aggregations, final Sensor sourceSensor, final Event source) {
        final List<AbstractSensorData> processedData = new ArrayList<>();

        for (final Aggregation aggregation : aggregations) {

            final List<AbstractSensorData> sensorDataList;
            if (Objects.nonNull(aggregation.getQuantity()) && Objects.nonNull(aggregation.getValidity())) {
                sensorDataList = sensorService.load(sourceSensor, aggregation.getQuantity(), aggregation.getValidity(), aggregation.getValidityTimeUnit());
            } else if (Objects.nonNull(aggregation.getQuantity())) {
                sensorDataList = sensorService.load(sourceSensor, aggregation.getQuantity());
            } else if (Objects.nonNull(aggregation.getValidity())) {
                sensorDataList = sensorService.load(sourceSensor, aggregation.getValidity(), aggregation.getValidityTimeUnit());
            } else {
                sensorDataList = sensorService.load(sourceSensor);
            }

            logger.debug("Loaded '{}' sensor data", sensorDataList.size());

            if(sensorDataList.isEmpty()) {
                return;
            }

            final Sensor eventSensor = sensorService.getEventSensor(sourceSensor.getInstance(), aggregation.getUidString());
            sensorDataList.forEach(sensorService::processing);

            switch (aggregation.getType()) {
                case NONE:
                    processedData.addAll(sensorDataList);
                    noAggregation(eventSensor, source);
                    break;
                case SUM:
                    processedData.addAll(sensorDataList);
                    sum(eventSensor, sourceSensor, sensorDataList);
                    break;
                case AVERAGE:
                    processedData.addAll(sensorDataList);
                    average(eventSensor, sourceSensor, sensorDataList);
                    break;
                case TREND:
                    processedData.addAll(sensorDataList);
                    trend(eventSensor, sensorDataList);
                    break;
                case MAX:
                    processedData.addAll(sensorDataList);
                    max(eventSensor, sourceSensor, sensorDataList);
                    break;
                case MIN:
                    processedData.addAll(sensorDataList);
                    min(eventSensor, sourceSensor, sensorDataList);
                    break;
                case COUNT:
                    count(eventSensor, sensorDataList, (CountingAggregation) aggregation, processedData);
                    break;
                default:
                    processedData.forEach(sensorService::persisted);
                    throw new IllegalStateException("Unexpected value: " + aggregation.getType());
            }

            logger.debug("Persisting '{}' sensor data", sensorDataList.size());
            sensorDataList.forEach(sensorService::persisted);
        }

        logger.debug("processed '{}' sensor data", processedData.size());
        processedData.forEach(sensorService::processed);
    }

    private <T extends Number> double calculateSetPoint(final Sensor sensor, final SetPointEvaluation analysis, final String inputStr) {
        final boolean inverse = analysis.getInverse();
        final SetPointController<T> controller;
        final T lower;
        final T upper;
        final T input;

        switch (sensor.getType()) {
            case INTEGER_NUMBER:
                lower = (T) MathHelper.convertToLong(analysis.getLowerSetPoint());
                upper = (T) MathHelper.convertToLong(analysis.getUpperSetPoint());
                input = (T) MathHelper.convertToLong(inputStr);
                break;
            case FLOATING_POINT_NUMBER:
                lower = (T) MathHelper.convertToDouble(analysis.getLowerSetPoint());
                upper = (T) MathHelper.convertToDouble(analysis.getUpperSetPoint());
                input = (T) MathHelper.convertToDouble(inputStr);
                break;
            default: throw new IllegalStateException("Unexpected value: " + sensor.getType());
        }

        controller = new SetPointController<>(inverse, lower, upper);
        final double result = controller.evaluate(input);
        logger.info("Evaluated SetPoint for input {} with result {}", inputStr, result);
        return result;
    }

    private void trend(final Sensor eventSensor, final List<AbstractSensorData> load) {
        final DoubleStream doubleStream = load.stream().mapToDouble(s -> MathHelper.convertToDouble(s.getData()));
        final Double average = doubleStream.average().orElseThrow();
        final Double last = doubleStream.skip(load.size() - 1).findFirst().orElseThrow();
        final Double result = last - average;
        publishAggregation(eventSensor, result);
    }

    private void noAggregation(final Sensor eventSensor, final Event source) {
        publishAggregation(eventSensor, source.getData());
    }

    private void sum(final Sensor eventSensor, final Sensor sourceSensor, final List<AbstractSensorData> sensorDataList) {

        final Object result;

        switch (sourceSensor.getType()) {
            case INTEGER_NUMBER:
                result = toLongStream(sensorDataList).sum();
                break;
            case FLOATING_POINT_NUMBER:
                result = toDoubleStream(sensorDataList).sum();
                break;

            default: throw new IllegalStateException("Unexpected value: " + sourceSensor.getType());
        }

        publishAggregation(eventSensor, result);
    }

    private void max(final Sensor eventSensor, final Sensor sourceSensor, final List<AbstractSensorData> sensorDataList) {

        final Object result;

        switch (sourceSensor.getType()) {
            case INTEGER_NUMBER:
                result = toLongStream(sensorDataList).max().orElseThrow();
                break;
            case FLOATING_POINT_NUMBER:
                result = toDoubleStream(sensorDataList).max().orElseThrow();
                break;

            default: throw new IllegalStateException("Unexpected value: " + sourceSensor.getType());
        }

        publishAggregation(eventSensor, result);
    }

    private void min(final Sensor eventSensor, final Sensor sourceSensor, final List<AbstractSensorData> sensorDataList) {

        final Object result;

        switch (sourceSensor.getType()) {
            case INTEGER_NUMBER:
                result = toLongStream(sensorDataList).min().orElseThrow();
                break;
            case FLOATING_POINT_NUMBER:
                result = toDoubleStream(sensorDataList).min().orElseThrow();
                break;

            default: throw new IllegalStateException("Unexpected value: " + sourceSensor.getType());
        }

        publishAggregation(eventSensor, result);
    }

    private void average(final Sensor eventSensor, final Sensor sourceSensor, final List<AbstractSensorData> sensorDataList) {

        final Object result;

        switch (sourceSensor.getType()) {
            case INTEGER_NUMBER:
                result = toLongStream(sensorDataList).average().orElseThrow();
                break;
            case FLOATING_POINT_NUMBER:
                result = toDoubleStream(sensorDataList).average().orElseThrow();
                break;

            default: throw new IllegalStateException("Unexpected value: " + sourceSensor.getType());
        }

        publishAggregation(eventSensor, result);
    }

    private void count(final Sensor eventSensor, final List<AbstractSensorData> sensorDataList, final CountingAggregation countingAggregation,
                       final List<AbstractSensorData> processedList) {

        ZonedDateTime localDateTime = ZonedDateTime.now().withNano(0);
        final Duration duration;

        switch (countingAggregation.getTimescaleUnit()) {
            case MINUTES:
                localDateTime = localDateTime.withSecond(0);
                break;
            case HOURS:
                localDateTime = localDateTime.withSecond(0).withMinute(0);
                break;
            case DAYS:
                localDateTime = localDateTime.withSecond(0).withMinute(0).withHour(0);
                break;
            case MONTHS:
                localDateTime = localDateTime.withSecond(0).withMinute(0).withHour(0).withDayOfMonth(1);
                break;
        }

        if (Objects.nonNull(countingAggregation.getTimescale()) && Objects.nonNull(countingAggregation.getTimescaleUnit())) {
            duration = Duration.of(countingAggregation.getTimescale(), countingAggregation.getTimescaleUnit());

            ZonedDateTime nextDateTime = localDateTime;
            ZonedDateTime processTillDateTime = localDateTime.minus(countingAggregation.getValidity(), countingAggregation.getValidityTimeUnit());
            logger.info("Counting for each {} {} from {} till {}",
                        countingAggregation.getTimescale(), countingAggregation.getTimescaleUnit(), processTillDateTime, localDateTime);

            do {
                countBetween(eventSensor, sensorDataList, nextDateTime, duration, countingAggregation.getQuantity(), processedList);
                nextDateTime = nextDateTime.minus(duration);
            } while (nextDateTime.isAfter(processTillDateTime));

        } else if (Objects.nonNull(countingAggregation.getValidity()) && Objects.nonNull(countingAggregation.getValidityTimeUnit())) {
            duration = Duration.of(countingAggregation.getValidity(), countingAggregation.getValidityTimeUnit());
            logger.info("Counting over the last {} {}", countingAggregation.getValidity(), countingAggregation.getValidityTimeUnit());
            countBetween(eventSensor, sensorDataList, localDateTime, duration, countingAggregation.getQuantity(), processedList);
        } else {
            publishAggregation(eventSensor, sensorDataList.size());
        }
    }

    private void countBetween(final Sensor eventSensor, final List<AbstractSensorData> sensorDataList,
                                 final ZonedDateTime till, final Duration duration, final Integer quantity,
                                 final List<AbstractSensorData> processedList) {
        final AtomicInteger count = new AtomicInteger();
        final ZonedDateTime from = till.minus(duration);
        final int threshold = Objects.nonNull(quantity) ? quantity : 1;

        logger.debug("Filtering through unprocessed sensor data between '{}' and '{}'", from, till);
        for (final AbstractSensorData data : sensorDataList) {
            if(data.getState() != ProcessingState.PROCESSED) {
                final ZonedDateTime dateTime = data.getCreatedAt();
                if(dateTime.isAfter(from) && dateTime.isBefore(till)) {
                    count.incrementAndGet();
                    processedList.add(data);
                }
            }
        }

        logger.debug("'{}' out of '{}' sensor data match our filter", count.get(), sensorDataList.size());


        if (count.get() >= threshold) {
            logger.info("Processed Monitor Event from {} with result '{}'", eventSensor, count.get());
            final AbstractSensorData sensorData = sensorService.store(eventSensor, till, count.get(), eventSensor.getAddress());
            eventService.createAnalyseEvent(eventSensor, sensorData);
        } else {
            logger.info("Processed Monitor Event from {}: '{}' elements do not meet threshold of '{}'", eventSensor, count.get(), threshold);
        }
    }

    private void publishAggregation(final Sensor eventSensor, final Object result) {
        logger.info("Processed Monitor Event from {} with result '{}'", eventSensor, result);
        final AbstractSensorData sensorData = sensorService.store(eventSensor, ZonedDateTime.now(), result, eventSensor.getAddress());
        eventService.createAnalyseEvent(eventSensor, sensorData);
    }

    private boolean evaluateMatchPolicy(final MatchPolicy policy, final Knowledge knowledge) {
        return evaluateMatchPolicy(policy, knowledge.getValue());
    }

    public boolean evaluateMatchPolicy(final MatchPolicy policy, final String valueStr) {
        final long value = MathHelper.convertToLong(valueStr);
        logger.debug("Evaluating {} {} {}", value, policy.getMatchType(), policy.getNumber());
        switch (policy.getMatchType()) {
            case SMALLER_THAN:
                return value < policy.getNumber();
            case SMALLER_OR_EQUAL:
                return value <= policy.getNumber();
            case EQUAL:
                return value == policy.getNumber();
            case GREATER_OR_EQUAL:
                return value >= policy.getNumber();
            case GREATER_THAN:
                return value > policy.getNumber();
        }
        return false;
    }

    private LongStream toLongStream(final List<AbstractSensorData> list) {
        return list.stream().map(s -> (LongSensorData) s).mapToLong(LongSensorData::getData);
    }

    private DoubleStream toDoubleStream(final List<AbstractSensorData> list) {
        return list.stream().map(s -> (DoubleSensorData) s).mapToDouble(DoubleSensorData::getData);
    }
}
