package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationSystemsListResponseDTO implements Serializable {

        //=================================================================================================
        // members
        
        private static final long serialVersionUID = 2359853742228146773L;
        
        private int count;
        private List<String> systems = new ArrayList<>();
                
        //=================================================================================================
        // methods
        
        //-------------------------------------------------------------------------------------------------
        public ConfigurationSystemsListResponseDTO() {}
        public ConfigurationSystemsListResponseDTO(int count,List<String> systems) {
            this.count = count;
            this.systems = systems;
        }
        
        //-------------------------------------------------------------------------------------------------
        public int getCount() { return count; }
        public List<String> getSystems() { return systems; }

        //-------------------------------------------------------------------------------------------------
        public void setCount(int count) { this.count = count; }
        public void setSystems(List<String> systems) { this.systems = systems; }
	
}
