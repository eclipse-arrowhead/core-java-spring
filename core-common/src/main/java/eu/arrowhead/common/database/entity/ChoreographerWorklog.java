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
    private ChoreographerStep session;

}
