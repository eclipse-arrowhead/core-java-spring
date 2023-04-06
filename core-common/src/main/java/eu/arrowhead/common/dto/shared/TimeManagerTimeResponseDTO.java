/********************************************************************************
 * Copyright (c) 2021 Lulea University of Technology
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Lulea University of Technology - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.shared;

import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;


public class TimeManagerTimeResponseDTO implements Serializable {

    //=================================================================================================
    // members
    
    private static final long serialVersionUID = 1648756742138137282L;

    private long epoch;
    private long epochMs;
    private String tz;
    private boolean dst;
    private boolean trusted;
            
    //=================================================================================================
    // methods
    
    //-------------------------------------------------------------------------------------------------
    public TimeManagerTimeResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
    public TimeManagerTimeResponseDTO(final String tz) {
        this.tz = tz;
        
        final Date date = new Date();
        epochMs = date.getTime();
        epoch = epochMs / 1000;

        dst = TimeZone.getTimeZone(this.tz).inDaylightTime(date);
        trusted = false;
    }

    //-------------------------------------------------------------------------------------------------
	public TimeManagerTimeResponseDTO(final String tz, final boolean trusted) {
        this.tz = tz;
        
        final Date date = new Date();
        epochMs = date.getTime();
        epoch = epochMs / 1000;

        dst = TimeZone.getTimeZone(this.tz).inDaylightTime(date);
        this.trusted = trusted;
    }

    //-------------------------------------------------------------------------------------------------
    public TimeManagerTimeResponseDTO(final long epoch, final String tz, final boolean trusted) {
        final Date date = new Date(epoch * 1000);
        this.epoch = epoch;
        this.epochMs = epoch * 1000;
        this.tz = tz;

        this.dst = TimeZone.getTimeZone(this.tz).inDaylightTime(date);
        this.trusted = trusted;
    }

    //-------------------------------------------------------------------------------------------------
    public TimeManagerTimeResponseDTO(final long epoch, final long epochMs, final String tz, final boolean dst, final boolean trusted) {
        this.epoch = epoch;
        this.epochMs = epochMs;
        this.tz = tz;
        this.dst = dst;
        this.trusted = trusted;
    }

    //-------------------------------------------------------------------------------------------------
    public long getEpoch() { return epoch; }
    public long getEpochMs() { return epochMs; }
    public String getTz() { return tz; }
    public boolean getDst() { return dst; }
    public boolean getTrusted() {return trusted; }

    //-------------------------------------------------------------------------------------------------
    public void setEpoch(final long epoch) { this.epoch = epoch; }
    public void setEpochMs(final long epochMs) { this.epochMs = epochMs; }
    public void setTz(final String tz) { this.tz = tz; }
    public void setDst(final boolean dst) { this.dst = dst; }
    public void setTrusted(final boolean trusted) { this.trusted = trusted; }
    
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}
