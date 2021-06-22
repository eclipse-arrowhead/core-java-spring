package eu.arrowhead.core.qos;

public class QosMonitorConstants {

	//=================================================================================================
	// members
	public static final String EVENT_QUEUE = "EventQueue";
	public static final String EVENT_BUFFER = "EventBuffer";
	public static final String EVENT_COLLECTOR = "EventCollector";

	public static final int EVENT_ARRAY_SIZE = 4;

	public static final String PROCESS_ID_KEY = "processID";

	public static final String RECEIVED_MONITORING_REQUEST_EVENT_PAYLOAD_SCHEMA = "[]";
	public static final int RECEIVED_MONITORING_REQUEST_EVENT_PAYLOAD_METADATA_SIZE = 1;
	public static final int RECEIVED_MONITORING_REQUEST_EVENT_POSITION = 0;

	public static final String STARTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_SCHEMA = "[]";
	public static final int STARTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_SIZE = 1;
	public static final int STARTED_MONITORING_MEASUREMENT_EVENT_POSITION = 1;

	public static final int FINISHED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_SIZE = 1;
	public static final int FINISHED_MONITORING_MEASUREMENT_EVENT_POSITION = 2;

	public static final String INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_SCHEMA = "[]";
	public static final int INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_MAX_SIZE = 3;
	public static final String INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_ROOT_CAUSE_KEY = "ROOT_CAUSE";
	public static final String INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_EXCEPTION_KEY = "EXCEPTION";
	public static final int INTERRUPTED_MONITORING_MEASUREMENT_EVENT_POSITION = 3;

	public static final String EXTERNAL_PING_MONITORING_SERVICE_INTERFACE = "HTTP-SECURE-JSON";
	public static final String EXTERNAL_PING_MONITORING_SERVICE_DEFINITION = "qos-icmp-ping";

	public static final String EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI = "/externalpingmonitorevent";

	public static final String QOS_MONITOR_REQUEST_MAX_RETRY = "request_max_retry";
	public static final String $QOS_MONITOR_REQUEST_MAX_RETRY_WD = "${" + QOS_MONITOR_REQUEST_MAX_RETRY + ":" + 3 + "}";

	public static final String QOS_MONITOR_REQUEST_SLEEP_PERIOD = "request_sleep_period";
	public static final String $QOS_MONITOR_REQUEST_SLEEP_PERIOD_WD = "${" + QOS_MONITOR_REQUEST_SLEEP_PERIOD + ":" + 1000 + "}";

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private QosMonitorConstants() {
		throw new UnsupportedOperationException();
	}
}
