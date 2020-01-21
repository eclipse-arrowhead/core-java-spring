package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.CoreDefaults;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
public class ChoreographerRunningStep {

    //=================================================================================================
    // members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = CoreDefaults.VARCHAR_BASIC, nullable = false)
    private String status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime startedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stepId", referencedColumnName = "id", nullable = false)
    private ChoreographerStep step;

    //=================================================================================================
    // methods

    //=================================================================================================
    public ChoreographerRunningStep() {}

    //=================================================================================================
    public ChoreographerRunningStep(String status, String message, ChoreographerStep step) {
        this.status = status;
        this.message = message;
        this.step = step;
    }

    //=================================================================================================
    public long getId() { return id; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public ZonedDateTime getStartedAt() { return startedAt; }
    public ChoreographerStep getStep() { return step; }

    //=================================================================================================

    public void setId(long id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public void setStartedAt(ZonedDateTime startedAt) { this.startedAt = startedAt; }
    public void setStep(ChoreographerStep step) { this.step = step; }

    //-------------------------------------------------------------------------------------------------
    @PrePersist
    public void onCreate() {
        this.startedAt = ZonedDateTime.now();
    }
}
