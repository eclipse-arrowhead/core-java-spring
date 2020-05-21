package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import eu.arrowhead.common.CoreDefaults;

@Entity
public class CaTrustedKey {
	
	//=================================================================================================
	// members

    public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = false, length = CoreDefaults.VARCHAR_EXTENDED)
    private String publicKey;

    @Column(nullable = false, unique = false, length = CoreDefaults.VARCHAR_BASIC)
    private String description;

    @Column(nullable = false, updatable = true, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime validAfter;

    @Column(nullable = false, updatable = true, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime validBefore;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public CaTrustedKey() {}

    //-------------------------------------------------------------------------------------------------
	public CaTrustedKey(final String publicKey, final String description) {
        this.publicKey = publicKey;
        this.description = description;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getPublicKey() { return publicKey; }
    public String getDescription() { return description; }
    public ZonedDateTime getValidAfter() { return validAfter; }
    public ZonedDateTime getValidBefore() { return validBefore; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setPublicKey(final String publicKey) { this.publicKey = publicKey; }
    public void setDescription(final String description) { this.description = description; }

    public void setValidAfter(final ZonedDateTime validAfter) { this.validAfter = validAfter; }
    public void setValidBefore(final ZonedDateTime validBefore) { this.validBefore = validBefore; }
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
