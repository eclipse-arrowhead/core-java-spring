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

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime entryDate;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String exception;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerWorklog() {
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerWorklog(final String message, final String exception) {
        this.message = message;
        this.exception = exception;
    }

    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }
    public ZonedDateTime getEntryDate() { return entryDate; }
    public String getMessage() { return message; }
    public String getException() { return exception; }

    //-------------------------------------------------------------------------------------------------
    public void setId(long id) { this.id = id; }
    public void setEntryDate(ZonedDateTime entryDate) { this.entryDate = entryDate; }
    public void setMessage(String message) { this.message = message; }
    public void setException(String exception) { this.exception = exception; }

    //-------------------------------------------------------------------------------------------------
    @PrePersist
    public void onCreate() { this.entryDate = ZonedDateTime.now(); }
}
