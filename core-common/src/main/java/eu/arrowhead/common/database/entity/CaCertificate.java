/********************************************************************************
 * Copyright (c) 2020 Evopro
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Evopro - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.CoreDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
public class CaCertificate {

    // =================================================================================================
    // members

    public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "commonName", "updatedAt", "createdAt");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = false, length = CoreDefaults.VARCHAR_BASIC)
    private String commonName;

    @Column(nullable = false, unique = true)
    private BigInteger serial;

    @Column(nullable = false, unique = false, length = CoreDefaults.VARCHAR_BASIC)
    private String createdBy;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime validAfter;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime validBefore;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    @Column(nullable = true, updatable = true)
    private ZonedDateTime revokedAt;

    // =================================================================================================
    // methods

    // -------------------------------------------------------------------------------------------------
    public CaCertificate() {
    }

    // -------------------------------------------------------------------------------------------------
    public CaCertificate(long id) {
        this.id = id;
    }

    // -------------------------------------------------------------------------------------------------
    public CaCertificate(final String commonName, final BigInteger serial, final String createdBy) {
        this.commonName = commonName;
        this.serial = serial;
        this.createdBy = createdBy;
    }

    // -------------------------------------------------------------------------------------------------
    public CaCertificate(final String commonName, final BigInteger serial, final String createdBy,
                         final ZonedDateTime validAfter, final ZonedDateTime validBefore) {
        this.commonName = commonName;
        this.serial = serial;
        this.createdBy = createdBy;
        this.validAfter = validAfter;
        this.validBefore = validBefore;
    }

    // -------------------------------------------------------------------------------------------------
    public long getId() {
        return id;
    }

    public String getCommonName() {
        return commonName;
    }

    public BigInteger getSerial() {
        return serial;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public ZonedDateTime getValidAfter() {
        return validAfter;
    }

    public ZonedDateTime getValidBefore() {
        return validBefore;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public ZonedDateTime getRevokedAt() {
        return revokedAt;
    }

    // -------------------------------------------------------------------------------------------------
    public void setId(final long id) {
        this.id = id;
    }

    public void setCommonName(final String commonName) {
        this.commonName = commonName;
    }

    public void setSerial(final BigInteger serial) {
        this.serial = serial;
    }

    public void setValidAfter(final ZonedDateTime validAfter) {
        this.validAfter = validAfter;
    }

    public void setValidBefore(final ZonedDateTime validBefore) {
        this.validBefore = validBefore;
    }

    public void setCreatedAt(final ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedAt(final ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setRevokedAt(final ZonedDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    // -------------------------------------------------------------------------------------------------
    @PrePersist
    public void onCreate() {
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = this.createdAt;
    }

    // -------------------------------------------------------------------------------------------------
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }
}
