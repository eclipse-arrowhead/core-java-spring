package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.CoreDefaults;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
public class ChoreographerWorklog {

    //=================================================================================================
    // members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = CoreDefaults.VARCHAR_BASIC, nullable = false)
    private String status;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime entryDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sessionId", referencedColumnName = "id", nullable = false)
    private ChoreographerSession session;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerWorklog() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerWorklog(ChoreographerSession session, String status, String message) {
        this.session = session;
        this.status = status;
        this.message = message;
    }

    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public ZonedDateTime getEntryDate() { return entryDate; }
    public ChoreographerSession getSession() { return session; }

    //-------------------------------------------------------------------------------------------------

    public void setId(long id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public void setEntryDate(ZonedDateTime entryDate) { this.entryDate = entryDate; }
    public void setSession(ChoreographerSession session) { this.session = session; }

    //-------------------------------------------------------------------------------------------------
    @PrePersist
    public void onCreate() { this.entryDate = ZonedDateTime.now(); }
}
