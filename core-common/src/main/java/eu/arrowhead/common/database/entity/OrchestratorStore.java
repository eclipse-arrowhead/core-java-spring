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
@Table (uniqueConstraints = {
		@UniqueConstraint(columnNames = {"serviceId", "consumerSystemId", "priority", "serviceInterfaceId"}),
		@UniqueConstraint(columnNames = {"serviceId", "consumerSystemId", "providerSystemId", "serviceInterfaceId"})
		})
public class OrchestratorStore {

	//=================================================================================================
	// members

	public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt", "priority"); //NOSONAR
	
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
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "serviceInterfaceId", referencedColumnName = "id", nullable = false)
	private ServiceInterface serviceInterface;
	
	@Column (nullable = false)
	private int priority;
	
	@Column (nullable = true, columnDefinition = "TEXT")
	private String attribute;
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStore() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStore(final ServiceDefinition serviceDefinition, final System consumerSystem, final System providerSystem,
			final Cloud providerCloud, final ServiceInterface serviceInterface, final Integer priority, final String attribute, final ZonedDateTime createdAt, final ZonedDateTime updatedAt) {
		super();
		this.serviceDefinition = serviceDefinition;
		this.consumerSystem = consumerSystem;
		this.providerSystem = providerSystem;
		this.providerCloud = providerCloud;
		this.serviceInterface = serviceInterface;
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
	public ServiceInterface serviceInterface() { return serviceInterface;}
	public int getPriority() { return priority; }
	public String getAttribute() { return attribute; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setServiceDefinition(final ServiceDefinition serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setConsumerSystem(final System consumerSystem) { this.consumerSystem = consumerSystem; }
	public void setProviderSystem(final System providerSystem) { this.providerSystem = providerSystem; }
	public void setProviderCloud(final Cloud providerCloud) { this.providerCloud = providerCloud; }
	public void setServiceInterface(final ServiceInterface serviceInterface) { this.serviceInterface = serviceInterface ;}
	public void setPriority(final int priority) { this.priority = priority; }
	public void setAttribute(final String attribute) { this.attribute = attribute; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

	//-------------------------------------------------------------------------------------------------	
	@Override
	public String toString() {
		return "OrchestratorStore [id=" + id + ", serviceDefinition=" + serviceDefinition + ", consumerSystem="
				+ consumerSystem + ", providerSystem=" + providerSystem + ", providerCloud=" + providerCloud
				+ ", serviceInterface=" + serviceInterface + ", priority=" + priority + ", attribute=" + attribute + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + "]";
	}
}