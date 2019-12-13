package eu.arrowhead.core.choreographer.database.service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerActionPlan;
import eu.arrowhead.common.database.entity.ChoreographerActionStep;
import eu.arrowhead.common.database.repository.ChoreographerActionActionStepConnectionRepository;
import eu.arrowhead.common.database.repository.ChoreographerActionPlanActionConnectionRepository;
import eu.arrowhead.common.database.repository.ChoreographerActionPlanRepository;
import eu.arrowhead.common.database.repository.ChoreographerActionRepository;
import eu.arrowhead.common.database.repository.ChoreographerActionStepRepository;
import eu.arrowhead.common.database.repository.ChoreographerActionStepServiceDefinitionConnectionRepository;
import eu.arrowhead.common.database.repository.ChoreographerNextActionStepRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.dto.internal.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.internal.ChoreographerActionStepRequestDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(SpringRunner.class)
public class ChoreographerDBServiceTest {
	
	//=================================================================================================
	// members

    @InjectMocks
    private ChoreographerDBService choreographerDBService;

    @Mock
    ChoreographerActionPlanRepository choreographerActionPlanRepository;

    @Mock
    ChoreographerActionPlanActionConnectionRepository choreographerActionPlanActionConnectionRepository;

    @Mock
    ChoreographerActionRepository choreographerActionRepository;

    @Mock
    ChoreographerActionActionStepConnectionRepository choreographerActionActionStepConnectionRepository;

    @Mock
    ChoreographerActionStepRepository choreographerActionStepRepository;

    @Mock
    ChoreographerActionStepServiceDefinitionConnectionRepository choreographerActionStepServiceDefinitionConnectionRepository;

    @Mock
    ChoreographerNextActionStepRepository choreographerNextActionStepRepository;

    @Mock
    ServiceDefinitionRepository serviceDefinitionRepository;
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getChoreographerActionPlanByIdTest() {
        final Optional<ChoreographerActionPlan> choreographerActionPlanOptional = Optional.of(getChoreographerActionPlan());
        when(choreographerActionPlanRepository.findById(anyLong())).thenReturn(choreographerActionPlanOptional);

        choreographerDBService.getChoreographerActionPlanById(1);
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void getChoreographerActionPlanByIdTestWithNotExistingId() {
        when(choreographerActionPlanRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
        choreographerDBService.getChoreographerActionPlanById(1);
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void getChoreographerActionPlanByIdWithInvalidId() {
        choreographerDBService.getChoreographerActionPlanById(-1);
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getChoreographerActionPlanEntriesResponseOKTest() {
        when(choreographerActionPlanRepository.findAll(any(PageRequest.class))).thenReturn(getPageOfChoreographerActionPlanList());

        choreographerDBService.getChoreographerActionPlanEntriesResponse(0, 10, Direction.ASC, "id");
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void getChoreographerActionPlanEntriesTestWithInvalidSortField() {
        when(choreographerActionPlanRepository.findAll(any(PageRequest.class))).thenReturn(getPageOfChoreographerActionPlanList());

        choreographerDBService.getChoreographerActionPlanEntriesResponse(0, 10, Sort.Direction.ASC, "notValid");
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void createChoreographerActionPlanTestWithNullInput() { 
		choreographerDBService.createChoreographerActionPlan(null, null);
	}

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void createChoreographerActionPlanTestWithEmptyActionRequestList() {
        choreographerDBService.createChoreographerActionPlan("actionPlan", getChoreographerActionRequestDTOEmptyListForTest());
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void createChoreographerActionPlanWithBlankName() {
        List<ChoreographerActionRequestDTO> actions = new ArrayList<>();
        actions.add(getChoreographerActionRequestDTOWithNextActionForTest(3, "testaction0", "testaction1"));
        actions.add(getChoreographerActionRequestDTOWithNextActionForTest(4, "testaction1", null));
        choreographerDBService.createChoreographerActionPlan("    ", actions);
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void removeActionPlanEntryByIdWithInvalidIdTest() {
        choreographerDBService.removeActionPlanEntryById(getInvalidIdForTest());
    }

    //-------------------------------------------------------------------------------------------------
    @Test(expected = InvalidParameterException.class)
    public void removeActionPlanEntryByIdWithIdNotInDBTest() {
        when(choreographerActionPlanRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

        choreographerDBService.removeActionPlanEntryById(getIdForTest());
    }
    
    //=================================================================================================
	// assistant methods
    
    //-------------------------------------------------------------------------------------------------
    private List<ChoreographerActionRequestDTO> getChoreographerActionRequestDTOEmptyListForTest() {
        return List.of();
    }

    //-------------------------------------------------------------------------------------------------
	private ChoreographerActionRequestDTO getChoreographerActionRequestDTOWithNextActionForTest(final int stepListSize, final String actionName, final String nextActionName) {
        List<ChoreographerActionStepRequestDTO> actionSteps = new ArrayList<>(stepListSize);
        for (int i = 0; i < stepListSize; ++i) {
            actionSteps.add(getChoreographerActionStepDTOForTest(ThreadLocalRandom.current().nextInt(0, 5), ThreadLocalRandom.current().nextInt(0, 5)));
        }

        return new ChoreographerActionRequestDTO(actionName, nextActionName, actionSteps);
    }

    //-------------------------------------------------------------------------------------------------
	private ChoreographerActionStepRequestDTO getChoreographerActionStepDTOForTest(final int usedServicesListSize, final int nextStepListSize) {
        String actionStepName = "testactionplan0";
        List<String> usedServiceNames = new ArrayList<>(usedServicesListSize);
        for (int i = 0; i < usedServicesListSize; ++i) {
            usedServiceNames.add("testservicedefinition" + i);
        }
        List<String> nextStepNames = new ArrayList<>(nextStepListSize);
        for (int i = 1; i <= nextStepListSize; ++i) {
            nextStepNames.add("testactionplan" + i);
        }
        
        return new ChoreographerActionStepRequestDTO(actionStepName, usedServiceNames, nextStepNames);
    }

    //-------------------------------------------------------------------------------------------------
	private ChoreographerActionPlan getChoreographerActionPlan() {
        final ChoreographerActionPlan actionPlan = new ChoreographerActionPlan("testactionplan0");
        actionPlan.setCreatedAt(getCreatedAtForTest());
        actionPlan.setUpdatedAt(getUpdatedAtForTest());
        actionPlan.setId(getIdForTest());

        return actionPlan;
    }

    //-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unused")
	private ChoreographerActionStep getChoreographerActionStep() {
        final ChoreographerActionStep actionStep = new ChoreographerActionStep("testactionstep0");
        actionStep.setId(getIdForTest());
        actionStep.setCreatedAt(getCreatedAtForTest());
        actionStep.setUpdatedAt(getUpdatedAtForTest());

        return actionStep;
    }

    //-------------------------------------------------------------------------------------------------
	private PageImpl<ChoreographerActionPlan> getPageOfChoreographerActionPlanList() {
        final List<ChoreographerActionPlan> choreographerActionPlanList = List.of(getChoreographerActionPlan());

        return new PageImpl<>(choreographerActionPlanList);
    }

    //-------------------------------------------------------------------------------------------------
	private long getIdForTest() {
        return 1L;
    }

    //-------------------------------------------------------------------------------------------------
	private long getInvalidIdForTest() {
        return -1L;
    }

    //-------------------------------------------------------------------------------------------------
	private ZonedDateTime getUpdatedAtForTest() {
        return Utilities.parseUTCStringToLocalZonedDateTime("2019-08-13 12:49:30");
    }

    //-------------------------------------------------------------------------------------------------
	private ZonedDateTime getCreatedAtForTest() {
        return Utilities.parseUTCStringToLocalZonedDateTime("2019-08-13 14:43:19");
    }
}