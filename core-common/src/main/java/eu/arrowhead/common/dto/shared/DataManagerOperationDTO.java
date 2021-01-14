package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataManagerOperationDTO implements Serializable {

        //=================================================================================================
        // members
        
        private static final long serialVersionUID = 2528826741322136689L;
        
        private String op;
        private String serviceName;
        private String serviceType;
                
        //=================================================================================================
        // methods
        
        //-------------------------------------------------------------------------------------------------
        public DataManagerOperationDTO() {}
        
        //-------------------------------------------------------------------------------------------------
        public String getOp() { return op; }

        //-------------------------------------------------------------------------------------------------
        public void setOp(String op) { this.op = new String(op); }
	
        //-------------------------------------------------------------------------------------------------
        public String getServiceName() { return serviceName; }

        //-------------------------------------------------------------------------------------------------
        public void setServiceName(String serviceName) { this.serviceName = new String(serviceName); }

        //-------------------------------------------------------------------------------------------------
        public String getServiceType() { return serviceType; }

        //-------------------------------------------------------------------------------------------------
        public void setServiceType(String serviceType) { this.serviceType = new String(serviceType); }
}
