package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;

import javax.persistence.CascadeType;
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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"actionStepId", "serviceDefinitionId"}))
public class ChoreographerActionStepServiceDefinitionConnection {

	//=================================================================================================
	// members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn (name = "actionStepId", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChoreographerActionStep actionStepEntry;

    @ManyToOne (fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn (name = "serviceDefinitionId", referencedColumnName = "id", nullable = false)
    private ServiceDefinition serviceDefinitionEntry;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionStepServiceDefinitionConnection() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionStepServiceDefinitionConnection(final ChoreographerActionStep actionStepEntry, final ServiceDefinition serviceDefinitionEntry) {
        this.actionStepEntry = actionStepEntry;
        this.serviceDefinitionEntry = serviceDefinitionEntry;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public ChoreographerActionStep getActionStepEntry() { return actionStepEntry; }
	public ServiceDefinition getServiceDefinitionEntry() { return serviceDefinitionEntry; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setActionStepEntry(final ChoreographerActionStep actionStepEntry) { this.actionStepEntry = actionStepEntry; }
    public void setServiceDefinitionEntry(final ServiceDefinition serviceDefinitionEntry) { this.serviceDefinitionEntry = serviceDefinitionEntry; }
    public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

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
}