package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table (uniqueConstraints = @UniqueConstraint(columnNames = {"serviceId", "consumerSystemId", "priority"}))
public class OrchestrationStore {

	//=================================================================================================
	// members

	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "serviceId", referencedColumnName = "id", nullable = false)
	private ServiceDefinition serviceDefinition;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "consumerSystemId", referencedColumnName = "id", nullable = false)
	private System consumerSystem;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "providerSystemId", referencedColumnName = "id", nullable = false)
	private System providerSystem; 
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "providerCloudId", referencedColumnName = "id", nullable = true)
	private Cloud providerCloud;
	
	@Column (nullable = true)
	private Integer priority;
	
	@Column (nullable = true, columnDefinition = "TEXT")
	private String attribute;
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestrationStore() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationStore(ServiceDefinition serviceDefinition, System consumerSystem, System providerSystem,
			Cloud providerCloud, Integer priority, String attribute, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
		super();
		this.serviceDefinition = serviceDefinition;
		this.consumerSystem = consumerSystem;
		this.providerSystem = providerSystem;
		this.providerCloud = providerCloud;
		this.priority = priority;
		this.attribute = attribute;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	//-------------------------------------------------------------------------------------------------
	@PrePersist
	public void onCreate() {
		this.createdAt = ZonedDateTime.now();
		this.updatedAt = this.createdAt;
	}
	
	//-------------------------------------------------------------------------------------------------
	@PreUpdate
	public void onUpdate() {
		this.updatedAt = ZonedDateTime.now();
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public ServiceDefinition getServiceDefinition() { return serviceDefinition;	}
	public System getConsumerSystem() {	return consumerSystem; }
	public System getProviderSystem() {	return providerSystem; }
	public Cloud getProviderCloud() { return providerCloud; }
	public Integer getPriority() { return priority; }
	public String getAttribute() { return attribute; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(long id) { this.id = id; }
	public void setServiceDefinition(ServiceDefinition serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setConsumerSystem(System consumerSystem) { this.consumerSystem = consumerSystem; }
	public void setProviderSystem(System providerSystem) { this.providerSystem = providerSystem; }
	public void setProviderCloud(Cloud providerCloud) { this.providerCloud = providerCloud; }
	public void setPriority(Integer priority) { this.priority = priority; }
	public void setAttribute(String attribute) { this.attribute = attribute; }
	public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

	//-------------------------------------------------------------------------------------------------	
	@Override
	public String toString() {
		return "OrchestrationStore [id=" + id + ", serviceDefinition=" + serviceDefinition + ", consumerSystem="
				+ consumerSystem + ", providerSystem=" + providerSystem + ", providerCloud=" + providerCloud
				+ ", priority=" + priority + ", attribute=" + attribute + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + "]";
	}
}