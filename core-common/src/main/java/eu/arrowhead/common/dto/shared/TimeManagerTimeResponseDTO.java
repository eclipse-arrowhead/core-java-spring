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
import java.io.Serializable;


public class TimeManagerTimeResponseDTO implements Serializable {


        //=================================================================================================
        // members
        
        private static final long serialVersionUID = 1648756742138137282L;

        private long epoch;
        private long epoch_ms;
        private String tz;
        private boolean inDaylightTime;
        private boolean trusted;
                
        //=================================================================================================
        // methods
        
        //-------------------------------------------------------------------------------------------------
        public TimeManagerTimeResponseDTO(final String tz) {
                this.tz = tz;
                
                Date date = new Date();
                epoch_ms = date.getTime();
                epoch = epoch_ms / 1000;

                inDaylightTime = TimeZone.getTimeZone(this.tz).inDaylightTime(date);
                trusted = false;
        }

        public TimeManagerTimeResponseDTO(final String tz, final boolean trusted) {
                this.tz = tz;
                
                Date date = new Date();
                epoch_ms = date.getTime();
                epoch = epoch_ms / 1000;

                inDaylightTime = TimeZone.getTimeZone(this.tz).inDaylightTime(date);
                this.trusted = trusted;
        }

        //-------------------------------------------------------------------------------------------------
        public TimeManagerTimeResponseDTO(final long epoch, final String tz, final boolean trusted) {
                Date date = new Date(epoch*1000);
                this.epoch = epoch;
                this.epoch_ms = epoch * 1000;
                this.tz = tz;

                inDaylightTime = TimeZone.getTimeZone(this.tz).inDaylightTime(date);
                this.trusted = trusted;
        }

        //-------------------------------------------------------------------------------------------------
        public TimeManagerTimeResponseDTO(final long epoch, final long epoch_ms, final String tz, final boolean inDaylightTime, final boolean trusted) {
                this.epoch = epoch;
                this.epoch_ms = epoch_ms;
                this.tz = tz;
                this.inDaylightTime = inDaylightTime;
                this.trusted = trusted;
        }

        //-------------------------------------------------------------------------------------------------
        public long getEpoch() { return epoch; }
        public long getEpoch_ms() { return epoch_ms; }
        public String getTz() { return tz; }
        public boolean getInDaylightTime() { return inDaylightTime; }
        public boolean getTrusted() {return trusted; }

        //-------------------------------------------------------------------------------------------------
        public void setEpoch(long epoch) { this.epoch = epoch; }
        public void setEpoch_ms(long epoch_ms) { this.epoch_ms = epoch_ms; }
        public void setTz(String tz) { this.tz = tz; }
        public void setInDaylightTime(boolean inDaylightTime) { this.inDaylightTime = inDaylightTime; }
        public void setTrusted(boolean trusted) { this.trusted = trusted; }
}
