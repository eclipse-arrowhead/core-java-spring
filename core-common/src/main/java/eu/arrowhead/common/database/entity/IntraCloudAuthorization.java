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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table (uniqueConstraints = @UniqueConstraint(columnNames = {"consumerSystemId", "providerSystemId", "serviceId"}))
public class IntraCloudAuthorization {

	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "consumerSystemId", referencedColumnName = "id", nullable = false)
	private System consumerSystem;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "providerSystemId", referencedColumnName = "id", nullable = false)
	private System providerSystem; 
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "serviceId", referencedColumnName = "id", nullable = false)
	private ServiceDefinition serviceDefinition;
	
	@Column (nullable = false, columnDefinition = "TIMESTAMP")
	private ZonedDateTime createdAt = ZonedDateTime.now();
	
	@Column (nullable = false, columnDefinition = "TIMESTAMP")
	private ZonedDateTime updatedAt = ZonedDateTime.now();

	public IntraCloudAuthorization() {
		
	}

	public IntraCloudAuthorization(System consumerSystem, System providerSystem, ServiceDefinition serviceDefinition) {
		this.consumerSystem = consumerSystem;
		this.providerSystem = providerSystem;
		this.serviceDefinition = serviceDefinition;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public System getConsumerSystem() {
		return consumerSystem;
	}

	public void setConsumerSystem(System consumerSystem) {
		this.consumerSystem = consumerSystem;
	}

	public System getProviderSystem() {
		return providerSystem;
	}

	public void setProviderSystem(System providerSystem) {
		this.providerSystem = providerSystem;
	}

	public ServiceDefinition getServiceDefinition() {
		return serviceDefinition;
	}

	public void setServiceDefinition(ServiceDefinition serviceDefinition) {
		this.serviceDefinition = serviceDefinition;
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public ZonedDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(ZonedDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return "IntraCloudAuthorization [id=" + id + ", consumerSystem=" + consumerSystem + ", providerSystem="
				+ providerSystem + ", serviceDefinition=" + serviceDefinition + "]";
	}
	
}
