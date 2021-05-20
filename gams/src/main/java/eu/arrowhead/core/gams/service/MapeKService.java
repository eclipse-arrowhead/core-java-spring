package eu.arrowhead.core.gams.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;

import eu.arrowhead.common.database.entity.AbstractAnalysis;
import eu.arrowhead.common.database.entity.AbstractSensorData;
import eu.arrowhead.common.database.entity.Aggregation;
import eu.arrowhead.common.database.entity.CountingAnalysis;
import eu.arrowhead.common.database.entity.DoubleSensorData;
import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.LongSensorData;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.entity.SetPointAnalysis;
import eu.arrowhead.core.gams.DataValidation;
import eu.arrowhead.core.gams.controller.SetPointController;
import eu.arrowhead.core.gams.dto.GamsPhase;
import eu.arrowhead.core.gams.dto.ProcessingState;
import eu.arrowhead.core.gams.rest.dto.SensorType;
import eu.arrowhead.core.gams.utility.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void publish(final Sensor sensor, final ZonedDateTime timestamp, final Object data, final String address) {
        validation.verify(sensor);

        final AbstractSensorData sensorData = sensorService.store(sensor, timestamp, data, address);
        logger.info(GamsPhase.MONITOR.getMarker(), "Received new sensor data: {}", sensorData);

        eventService.createMonitorEvent(sensor, sensorData);
    }

    public void monitor(final Event source) {
        validation.verify(source);
        logger.info(source.getMarker(), "Received monitor source: {}", source.shortToString());

        final Sensor eventSensor = sensorService.getEventSensor(source.getSensor().getInstance());
        final Sensor sourceSensor = source.getSensor();
        final List<Aggregation> preAnalysis = knowledgeService.loadPreAnalysis(sourceSensor);

        if (sourceSensor.getType() == SensorType.EVENT) {
            noAggregation(eventSensor, source);
        } else if (preAnalysis.isEmpty()) {
            noAggregation(eventSensor, source);
        } else {
            processAggregation(knowledgeService.loadPreAnalysis(sourceSensor), eventSensor, sourceSensor, source);
        }
    }

    public void analyze(final Event source) {
        validation.verify(source);
        logger.info(source.getMarker(), "Received analyze event: {}", source::shortToString);

        timeoutService.rescheduleTimeoutEvent(source);
        final Sensor sensor = source.getSensor();

        final List<AbstractAnalysis> analyses = knowledgeService.loadAnalysis(source.getSensor());
        for (AbstractAnalysis analysis : analyses) {
            switch(analysis.getType()) {
                case SET_POINT:
                    final double setPoint = calculateSetPoint(sensor, (SetPointAnalysis) analysis, source.getData());
                    knowledgeService.put(sensor.getInstance(), analysis.getKnowledgeName(), String.valueOf(setPoint));
                    eventService.createPlanEvent(source, analysis.getKnowledgeName(), String.valueOf(setPoint));

                    break;
                case COUNTING:
                    final CountingAnalysis countingAnalysis = (CountingAnalysis) analysis;
                    final ZonedDateTime validFrom = ZonedDateTime.now().minus(countingAnalysis.getTimeValue(),countingAnalysis.getTimeUnit());
                    final long count = sensorService.count(sensor, validFrom);

                    if(count >= countingAnalysis.getCount()) {
                        knowledgeService.put(sensor.getInstance(), analysis.getKnowledgeName(), String.valueOf(count));
                        eventService.createPlanEvent(source, analysis.getKnowledgeName(), String.valueOf(count));
                    }
                    break;
                default: throw new IllegalStateException("Unexpected value: " + analysis.getType());
            }
        }
    }

    private <T extends Number> double calculateSetPoint(final Sensor sensor, final SetPointAnalysis analysis, final String inputStr) {
        final boolean inverse = analysis.getInverse();
        final SetPointController<T> controller;
        final T lower;
        final T upper;
        final T input;

        switch(sensor.getType()) {
            case INTEGER_NUMBER:
                lower = (T)MathHelper.convertToLong(analysis.getLowerSetPoint());
                upper = (T)MathHelper.convertToLong(analysis.getUpperSetPoint());
                input = (T)MathHelper.convertToLong(inputStr);
                break;
            case FLOATING_POINT_NUMBER:
                lower = (T)MathHelper.convertToDouble(analysis.getLowerSetPoint());
                upper = (T)MathHelper.convertToDouble(analysis.getUpperSetPoint());
                input = (T)MathHelper.convertToDouble(inputStr);
                break;
            default: throw new IllegalStateException("Unexpected value: " + sensor.getType());
        }

        controller = new SetPointController<>(inverse,lower,upper);
        return controller.evaluate(input);
    }

    public void plan(final Event event) {
        validation.verify(event);
        logger.info(event.getMarker(), "Received plan event: {}", event::shortToString);


    }

    public void execute(final Event event) {
        validation.verify(event);
        logger.info(event.getMarker(), "Received execute event: {}", event::shortToString);
        final Runnable actionPlan = apiCallService.assembleActionPlan(event.getSensor().getInstance(), event);
        executorService.submit(actionPlan);
    }

    public void failure(final Event event) {
        logger.fatal(GamsPhase.FAILURE.getMarker(), event);
    }

    private void processAggregation(final List<Aggregation> preAnalysis, final Sensor eventSensor, final Sensor sourceSensor, final Event source) {
        final List<AbstractSensorData> processedData = new ArrayList<>();

        for (final Aggregation aggregation : preAnalysis) {
            final List<AbstractSensorData> load = sensorService.load(sourceSensor, aggregation.getQuantity());

            switch (aggregation.getType()) {
                case NONE:
                    processedData.addAll(load);
                    noAggregation(eventSensor, source);
                    break;
                case SUM:
                    processedData.addAll(load);
                    summary(eventSensor, sourceSensor, load);
                    break;
                case AVERAGE:
                    processedData.addAll(load);
                    average(eventSensor, sourceSensor, load);
                    break;
                case TREND:
                    processedData.addAll(load);
                    trend(eventSensor, load);
                    break;
                case MAX:
                    processedData.addAll(load);
                    max(eventSensor, sourceSensor, load);
                    break;
                case MIN:
                    processedData.addAll(load);
                    min(eventSensor, sourceSensor, load);
                    break;
                default: throw new IllegalStateException("Unexpected value: " + aggregation.getType());
            }
        }

        for (final AbstractSensorData datum : processedData) {
            datum.setState(ProcessingState.PROCESSED);
            sensorService.store(datum);
        }
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

    private void summary(final Sensor eventSensor, final Sensor sourceSensor, final List<AbstractSensorData> sensorDataList) {

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

    private void publishAggregation(final Sensor eventSensor, final Object result) {
        final AbstractSensorData sensorData = sensorService.store(eventSensor, ZonedDateTime.now(), result, eventSensor.getAddress());
        eventService.createAnalyseEvent(eventSensor, sensorData);
    }

    private LongStream toLongStream(final List<AbstractSensorData> list) {
        return list.stream().map(s -> (LongSensorData) s).mapToLong(LongSensorData::getData);
    }

    private DoubleStream toDoubleStream(final List<AbstractSensorData> list) {
        return list.stream().map(s -> (DoubleSensorData) s).mapToDouble(DoubleSensorData::getData);
    }
}
