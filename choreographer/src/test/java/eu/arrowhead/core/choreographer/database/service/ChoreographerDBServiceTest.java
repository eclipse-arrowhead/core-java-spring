/********************************************************************************
 * Copyright (c) 2020 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.choreographer.database.service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.repository.ChoreographerActionRepository;
import eu.arrowhead.common.database.repository.ChoreographerPlanRepository;
import eu.arrowhead.common.database.repository.ChoreographerStepRepository;
import eu.arrowhead.common.dto.internal.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.internal.ChoreographerStepRequestDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.data.domain.Sort.Direction;

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
    ChoreographerPlanRepository choreographerPlanRepository;
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getPlanByIdTest() {
        final Optional<ChoreographerPlan> choreographerPlanOptional = Optional.of(getPlan());
        when(choreographerPlanRepository.findById(anyLong())).thenReturn(choreographerPlanOptional);

        choreographerDBService.getPlanById(1);
    }


    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void getPlanByIdTestWithNotExistingId() {
        when(choreographerPlanRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
        choreographerDBService.getPlanById(1);
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void getPlanByIdWithInvalidId() {
        choreographerDBService.getPlanById(-1);
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getPlanEntriesResponseOKTest() {
        when(choreographerPlanRepository.findAll(any(PageRequest.class))).thenReturn(getPageOfChoreographerPlanList());

        choreographerDBService.getPlanEntriesResponse(0, 10, Direction.ASC, "id");
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void getPlanEntriesTestWithInvalidSortField() {
        when(choreographerPlanRepository.findAll(any(PageRequest.class))).thenReturn(getPageOfChoreographerPlanList());

        choreographerDBService.getPlanEntriesResponse(0, 10, Direction.ASC, "notValid");
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void createPlanTestWithNullInput() {
		choreographerDBService.createPlan(null, null, null);
	}

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void createPlanTestWithEmptyActionRequestList() {
        choreographerDBService.createPlan("actionPlan", null, getActionRequestDTOEmptyListForTest());
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void createPlanWithBlankName() {
        List<ChoreographerActionRequestDTO> actions = new ArrayList<>();
        actions.add(getActionRequestDTOWithNextActionForTest(3, "testaction0", "testaction1"));
        actions.add(getActionRequestDTOWithNextActionForTest(4, "testaction1", null));
        choreographerDBService.createPlan("    ", "testaction0", actions);
    }

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
    public void removePlanEntryByIdWithInvalidIdTest() {
        choreographerDBService.removePlanEntryById(getInvalidIdForTest());
    }

    //-------------------------------------------------------------------------------------------------
    @Test(expected = InvalidParameterException.class)
    public void removePlanEntryByIdWithIdNotInDBTest() {
        when(choreographerPlanRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

        choreographerDBService.removePlanEntryById(getIdForTest());
    }
    
    //=================================================================================================
	// assistant methods
    
    //-------------------------------------------------------------------------------------------------
    private List<ChoreographerActionRequestDTO> getActionRequestDTOEmptyListForTest() {
        return List.of();
    }

    //-------------------------------------------------------------------------------------------------
	private ChoreographerActionRequestDTO getActionRequestDTOWithNextActionForTest(final int stepListSize, final String actionName, final String nextActionName) {
        List<ChoreographerStepRequestDTO> steps = new ArrayList<>(stepListSize);
        for (int i = 0; i < stepListSize; ++i) {
            steps.add(getChoreographerStepDTOForTest(ThreadLocalRandom.current().nextInt(0, 5)));
        }

        List<String> firstStepNames = new ArrayList<>();
        firstStepNames.add("testactionstep0");

        return new ChoreographerActionRequestDTO(actionName, nextActionName, firstStepNames, steps);
    }

    //-------------------------------------------------------------------------------------------------
	private ChoreographerStepRequestDTO getChoreographerStepDTOForTest(final int nextStepListSize) {
        String stepName = "testactionstep0";
        String serviceName = "testservice0";

        List<String> nextStepNames = new ArrayList<>(nextStepListSize);
        for (int i = 1; i <= nextStepListSize; ++i) {
            nextStepNames.add("testplanstep" + i);
        }

        return new ChoreographerStepRequestDTO(stepName, serviceName, nextStepNames, 1);
    }

    //-------------------------------------------------------------------------------------------------
	private ChoreographerPlan getPlan() {
        final ChoreographerPlan plan = new ChoreographerPlan("testplan0");
        plan.setCreatedAt(getCreatedAtForTest());
        plan.setUpdatedAt(getUpdatedAtForTest());
        plan.setId(getIdForTest());
        plan.setFirstAction(new ChoreographerAction("testaction0", null));

        return plan;
    }

    //-------------------------------------------------------------------------------------------------
	private PageImpl<ChoreographerPlan> getPageOfChoreographerPlanList() {
        final List<ChoreographerPlan> choreographerPlanList = List.of(getPlan());

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
}