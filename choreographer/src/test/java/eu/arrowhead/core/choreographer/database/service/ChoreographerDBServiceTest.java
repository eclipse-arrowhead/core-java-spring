package eu.arrowhead.core.choreographer.database.service;

import org.junit.runner.RunWith;

import static org.mockito.ArgumentMatchers.any;

import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ChoreographerDBServiceTest {
    /*
	
	//=================================================================================================
	// members

    @InjectMocks
    private ChoreographerDBService choreographerDBService;

    @Mock
    ChoreographerPlanRepository choreographerPlanRepository;

    @Mock
    ChoreographerActionPlanActionConnectionRepository choreographerActionPlanActionConnectionRepository;

    @Mock
    ChoreographerActionRepository choreographerActionRepository;

    @Mock
    ChoreographerActionActionStepConnectionRepository choreographerActionActionStepConnectionRepository;

    @Mock
    ChoreographerStepRepository choreographerStepRepository;

    @Mock
    ChoreographerStepServiceDefinitionConnectionRepository choreographerStepServiceDefinitionConnectionRepository;

    @Mock
    ChoreographerNextStepRepository choreographerNextStepRepository;

    @Mock
    ServiceDefinitionRepository serviceDefinitionRepository;
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getChoreographerActionPlanByIdTest() {
        final Optional<ChoreographerPlan> choreographerActionPlanOptional = Optional.of(getChoreographerActionPlan());
        when(choreographerPlanRepository.findById(anyLong())).thenReturn(choreographerActionPlanOptional);

        choreographerDBService.getPlanById(1);
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void getChoreographerActionPlanByIdTestWithNotExistingId() {
        when(choreographerPlanRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
        choreographerDBService.getPlanById(1);
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void getChoreographerActionPlanByIdWithInvalidId() {
        choreographerDBService.getPlanById(-1);
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getChoreographerActionPlanEntriesResponseOKTest() {
        when(choreographerPlanRepository.findAll(any(PageRequest.class))).thenReturn(getPageOfChoreographerActionPlanList());

        choreographerDBService.getChoreographerActionPlanEntriesResponse(0, 10, Direction.ASC, "id");
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void getChoreographerActionPlanEntriesTestWithInvalidSortField() {
        when(choreographerPlanRepository.findAll(any(PageRequest.class))).thenReturn(getPageOfChoreographerActionPlanList());

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
        when(choreographerPlanRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

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
	private ChoreographerPlan getChoreographerActionPlan() {
        final ChoreographerPlan actionPlan = new ChoreographerPlan("testactionplan0");
        actionPlan.setCreatedAt(getCreatedAtForTest());
        actionPlan.setUpdatedAt(getUpdatedAtForTest());
        actionPlan.setId(getIdForTest());

        return actionPlan;
    }

    //-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unused")
	private ChoreographerStep getChoreographerActionStep() {
        final ChoreographerStep actionStep = new ChoreographerStep("testactionstep0");
        actionStep.setId(getIdForTest());
        actionStep.setCreatedAt(getCreatedAtForTest());
        actionStep.setUpdatedAt(getUpdatedAtForTest());

        return actionStep;
    }

    //-------------------------------------------------------------------------------------------------
	private PageImpl<ChoreographerPlan> getPageOfChoreographerActionPlanList() {
        final List<ChoreographerPlan> choreographerPlanList = List.of(getChoreographerActionPlan());

        return new PageImpl<>(choreographerPlanList);
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
     */
}