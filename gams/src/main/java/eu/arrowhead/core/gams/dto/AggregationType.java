package eu.arrowhead.core.gams.dto;

public enum AggregationType {

    /* sum of sensor data to be included in the event */
    SUM,

    /* the average value of sensor data to be included in the event */
    AVERAGE,

    /* a negative or positive trend over the sensor data */
    TREND,

    /* max */
    MAX,

    /* min */
    MIN,

    /* count */
    COUNT,

    /* create event in any case */
    NONE,

    SEPARATE;
}
