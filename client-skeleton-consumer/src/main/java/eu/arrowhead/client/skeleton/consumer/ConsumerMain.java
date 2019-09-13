package eu.arrowhead.client.skeleton.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import eu.arrowhead.client.skeleton.common.ArrowheadService;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO.Builder;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;

@SpringBootApplication
@ComponentScan(basePackages = {CommonConstants.BASE_PACKAGE}) //TODO: add custom packages if any
public class ConsumerMain implements ApplicationRunner {
    public static void main( final String[] args ) {
    	SpringApplication.run(ConsumerMain.class, args);
    }
    
    //-------------------------------------------------------------------------------------------------
    @Autowired
	private ArrowheadService arrowheadService;
    
 	//-------------------------------------------------------------------------------------------------
    @Override
	public void run(ApplicationArguments args) throws Exception {
		//Example of initiating an orchestration
    	Builder orchestrationFormBuilder = arrowheadService.getOrchestrationFormBuilder();
    	
    	ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
    	requestedService.setServiceDefinitionRequirement("Test-Service");
    	orchestrationFormBuilder.requestedService(requestedService);
    	
    	OrchestrationFormRequestDTO orchestrationRequest = orchestrationFormBuilder.build();
    	ResponseEntity<OrchestrationResponseDTO> response = arrowheadService.proceedOrchestration(orchestrationRequest);
    	
    	OrchestrationResponseDTO orchestrationResponse = null;
    	if (response == null || response.getStatusCode() != HttpStatus.OK) {
    		//Handle the unsuccessful request as you wish
    	} else {
    		orchestrationResponse = response.getBody();
    	}
    	
    	
    	//Example of consuming the service from the chosen provider
    	
	}
}
