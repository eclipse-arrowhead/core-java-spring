package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import eu.arrowhead.common.CoreDefaults;

// Formerly known as "Plan(s)"
@Entity
public class ChoreographerAction {
	
	//=================================================================================================
	// members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = CoreDefaults.VARCHAR_BASIC)
    private String actionName;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "nextActionId", referencedColumnName = "id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChoreographerAction nextAction;

    @OneToMany (mappedBy = "actionEntry", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerActionPlanActionConnection> actionPlanActionConnections = new HashSet<>();

    @OneToMany (mappedBy = "actionEntry", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerActionActionStepConnection> actionActionStepConnections = new HashSet<>();

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerAction() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerAction(final String actionName, final ChoreographerAction nextAction) {
        this.actionName = actionName;
        this.nextAction = nextAction;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getActionName() { return actionName; }
	public ChoreographerAction getNextAction() { return nextAction; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
	public Set<ChoreographerActionPlanActionConnection> getActionPlanActionConnections() { return actionPlanActionConnections; }
	public Set<ChoreographerActionActionStepConnection> getActionActionStepConnections() { return actionActionStepConnections; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setActionName(final String actionName) { this.actionName = actionName; }
    public void setNextAction(final ChoreographerAction nextAction) { this.nextAction = nextAction; }
    public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setActionPlanActionConnections(final Set<ChoreographerActionPlanActionConnection> actionPlanActionConnections) { this.actionPlanActionConnections = actionPlanActionConnections; }
    public void setActionActionStepConnections(final Set<ChoreographerActionActionStepConnection> actionActionStepConnections) { this.actionActionStepConnections = actionActionStepConnections; }

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