package eu.arrowhead.common.dto.shared;

import java.util.Date;
import java.util.TimeZone;
import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;

public class TimeManagerTimeResponseDTO implements Serializable {

        //=================================================================================================
        // members
        
        private static final long serialVersionUID = 1248856742138137282L;

        private long epoch;
        private long epoch_ms;
        private String tz;
        private boolean inDaylightTime;
                
        //=================================================================================================
        // methods
        
        //-------------------------------------------------------------------------------------------------
        public TimeManagerTimeResponseDTO() {
                Date date = new Date();
                epoch_ms = date.getTime();
                epoch = epoch_ms / 1000;

                tz = new String("Europe/London");
                inDaylightTime = TimeZone.getTimeZone(this.tz).inDaylightTime(date);
        }

        public TimeManagerTimeResponseDTO(final long epoch, final String tz) {
                Date date = new Date(epoch*1000);
                this.epoch = epoch;
                this.epoch_ms = epoch * 1000;
                this.tz = tz;

                inDaylightTime = TimeZone.getTimeZone(this.tz).inDaylightTime(date);
        }

        public void setEpoch(long epoch) {
                this.epoch = epoch;
        }
            
        public long getEpoch() {
                return epoch;
        }

        public void setEpoch_ms(long epoch_ms) {
                this.epoch_ms = epoch_ms;
        }
            
        public long getEpoch_ms() {
                return epoch_ms;
        }

        public void setTz(String tz) {
                this.tz = tz;
        }
            
        public String getTz() {
                return tz;
        }

        public void setInDaylightTime(boolean inDaylightTime) {
                this.inDaylightTime = inDaylightTime;
        }
            
        public boolean getInDaylightTime() {
                return inDaylightTime;
        }
	
}
