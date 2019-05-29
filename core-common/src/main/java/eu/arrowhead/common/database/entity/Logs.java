package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import eu.arrowhead.common.Defaults;

@Entity
public class Logs {
	
	@Id
	@Column (length = Defaults.VARCHAR_LOG)
	private String logId;
	
	@Column (nullable = true, columnDefinition = "TIMESTAMP DEFAULT NULL")
	private ZonedDateTime entryDate;
	
	@Column (nullable = true, length = Defaults.VARCHAR_LOG)
	private String logger;
	
	@Column (nullable = true, length = Defaults.VARCHAR_LOG)
	private String logLevel;
	
	@Column (nullable = true, columnDefinition = "TEXT")
	private String message;
	
	@Column (nullable = true, columnDefinition = "TEXT")
	private String exception;

	public Logs() {
	
	}

	public Logs(final String logId, final ZonedDateTime entryDate, final String logger, final String logLevel, final String message, final String exception) {
		this.logId = logId;
		this.entryDate = entryDate;
		this.logger = logger;
		this.logLevel = logLevel;
		this.message = message;
		this.exception = exception;
	}

	public String getLogId() {
		return logId;
	}

	public void setLogId(final String logId) {
		this.logId = logId;
	}

	public ZonedDateTime getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(final ZonedDateTime entryDate) {
		this.entryDate = entryDate;
	}

	public String getLogger() {
		return logger;
	}

	public void setLogger(final String logger) {
		this.logger = logger;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(final String logLevel) {
		this.logLevel = logLevel;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public String getException() {
		return exception;
	}

	public void setException(final String exception) {
		this.exception = exception;
	}

	@Override
	public String toString() {
		return "Logs [logId=" + logId + ", entryDate=" + entryDate + ", logger=" + logger + ", logLevel=" + logLevel
				+ ", message=" + message + "]";
	}
	
}
