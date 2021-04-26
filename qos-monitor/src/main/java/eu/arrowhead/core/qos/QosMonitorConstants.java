package eu.arrowhead.core.qos;

public class QosMonitorConstants {

	//=================================================================================================
	// members

	public static final String RECEIVED_MONITORING_REQUEST_QUEUE = "ReceivedMonitoringRequestEventQueue";
	public static final String STARTED_MONITORING_MEASUREMENT_QUEUE = "StartedMonitoringMeasurementEventQueue";
	public static final String FINISHED_MONITORING_MEASUREMENT_QUEUE = "FinishedMonitoringMeasurementEventQueue";
	public static final String INTERUPTED_MONITORING_MEASUREMENT_QUEUE = "InteruptedMonitoringMeasurementEventQueue";


	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private QosMonitorConstants() {
		throw new UnsupportedOperationException();
	}
}
