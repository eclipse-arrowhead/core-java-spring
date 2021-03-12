/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.database.entity;

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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"systemId", "deviceId"}))
public class SystemRegistry {

    //=================================================================================================
    // members
    public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt"); //NOSONAR

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "systemId", referencedColumnName = "id", nullable = false)
    private System system;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "deviceId", referencedColumnName = "id", nullable = false)
    private Device device;

    @Column(nullable = true)
    private ZonedDateTime endOfValidity;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = true)
    private Integer version = 1;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public SystemRegistry() {
    }

    //-------------------------------------------------------------------------------------------------
    public SystemRegistry(final System system, final Device device, final ZonedDateTime endOfValidity,
                          final String metadata, final Integer version) {
        this.system = system;
        this.device = device;
        this.endOfValidity = endOfValidity;
        this.metadata = metadata;
        this.version = version;
    }

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

    //-------------------------------------------------------------------------------------------------
    public long getId() {
        return id;
    }

    public System getSystem() {
        return system;
    }

    public Device getDevice() {
        return device;
    }

    public ZonedDateTime getEndOfValidity() {
        return endOfValidity;
    }

    public String getMetadata() {
        return metadata;
    }

    public Integer getVersion() {
        return version;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    //-------------------------------------------------------------------------------------------------
    public void setId(final long id) {
        this.id = id;
    }

    public void setSystem(final System system) {
        this.system = system;
    }

    public void setDevice(final Device device) {
        this.device = device;
    }

    public void setEndOfValidity(final ZonedDateTime endOfValidity) {
        this.endOfValidity = endOfValidity;
    }

    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }

    public void setCreatedAt(final ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(final ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return "ServiceRegistry [id = " + id + ", system = " + system + ", device = " + device + ", endOfValidity = " + endOfValidity + ", version = " + version + "]";
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final SystemRegistry other = (SystemRegistry) obj;

        return id == other.id;
    }
}