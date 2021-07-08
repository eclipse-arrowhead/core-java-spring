package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataManagerServicesResponseDTO implements Serializable {

        //=================================================================================================
        // members
        
        private static final long serialVersionUID = 2184859722224129210L;
        
        private List<String> services= new ArrayList<>();
                
        //=================================================================================================
        // methods
        
        //-------------------------------------------------------------------------------------------------
        public DataManagerServicesResponseDTO() {}
        
        //-------------------------------------------------------------------------------------------------
        public List<String> getServices() { return services; }

        //-------------------------------------------------------------------------------------------------
        public void setServices(List<String> services) { this.services = services; }

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
