package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import eu.arrowhead.common.CoreDefaults;

@Entity
public class ChoreographerActionPlan {
	
	//=================================================================================================
	// members

    public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = CoreDefaults.VARCHAR_BASIC)
    private String actionPlanName;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    @OneToMany (mappedBy = "actionPlanEntry", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerActionPlanActionConnection> actionPlanActionConnections = new HashSet<>();

    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionPlan() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionPlan(final String actionPlanName) {
        this.actionPlanName = actionPlanName;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getActionPlanName() { return actionPlanName; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
	public Set<ChoreographerActionPlanActionConnection> getActionPlanActionConnections() { return actionPlanActionConnections; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setActionPlanName(final String actionPlanName) { this.actionPlanName = actionPlanName; }
    public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setActionPlanActionConnections(final Set<ChoreographerActionPlanActionConnection> actionPlanActionConnections) { this.actionPlanActionConnections = actionPlanActionConnections; }

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