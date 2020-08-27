package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataManagerSystemsResponseDTO implements Serializable {

        //=================================================================================================
        // members
        
        private static final long serialVersionUID = 1588856742128136289L;
        
        private List<String> systems = new ArrayList<>();
                
        //=================================================================================================
        // methods
        
        //-------------------------------------------------------------------------------------------------
        public DataManagerSystemsResponseDTO() {}
        
        //-------------------------------------------------------------------------------------------------
        public List<String> getSystems() { return systems; }

        //-------------------------------------------------------------------------------------------------
        public void setSystems(List<String> systems) { this.systems = systems; }
	
}
