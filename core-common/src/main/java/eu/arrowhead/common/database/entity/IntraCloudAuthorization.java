package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.List;

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
@Table (uniqueConstraints = @UniqueConstraint(columnNames = {"consumerSystemId", "providerSystemId", "serviceId"}))
public class IntraCloudAuthorization {
	
	//=================================================================================================
	// members
	
	public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt"); //NOSONAR

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
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public IntraCloudAuthorization() {}

	//-------------------------------------------------------------------------------------------------
	public IntraCloudAuthorization(final System consumerSystem, final System providerSystem, final ServiceDefinition serviceDefinition) {
		this.consumerSystem = consumerSystem;
		this.providerSystem = providerSystem;
		this.serviceDefinition = serviceDefinition;
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
	public System getConsumerSystem() { return consumerSystem; }
	public System getProviderSystem() { return providerSystem; }
	public ServiceDefinition getServiceDefinition() { return serviceDefinition; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setConsumerSystem(final System consumerSystem) { this.consumerSystem = consumerSystem; }
	public void setProviderSystem(final System providerSystem) { this.providerSystem = providerSystem; }
	public void setServiceDefinition(final ServiceDefinition serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "IntraCloudAuthorization [id = " + id + ", consumerSystem = " + consumerSystem + ", providerSystem = " + providerSystem + ", serviceDefinition = " + serviceDefinition + "]";
	}
}