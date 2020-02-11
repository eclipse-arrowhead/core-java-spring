package eu.arrowhead.common.database.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"actionId", "actionStepId"}))
public class ChoreographerActionActionStepConnection {
	
	//=================================================================================================
	// members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn (name = "actionStepId", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChoreographerActionStep actionStepEntry;

    @ManyToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn (name = "actionId", referencedColumnName = "id", nullable = false)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private ChoreographerAction actionEntry;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionActionStepConnection() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionActionStepConnection(final ChoreographerActionStep actionStepEntry, final ChoreographerAction actionEntry) {
        this.actionStepEntry = actionStepEntry;
        this.actionEntry = actionEntry;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public ChoreographerActionStep getActionStepEntry() { return actionStepEntry; }
	public ChoreographerAction getActionEntry() { return actionEntry; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setActionStepEntry(final ChoreographerActionStep actionStepEntry) { this.actionStepEntry = actionStepEntry; }
    public void setActionEntry(final ChoreographerAction actionEntry) { this.actionEntry = actionEntry; }
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