package eu.arrowhead.core.choreographer.executor;

import java.util.ArrayList;
import java.util.List;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;

public class ExecutorData {
	
	//=================================================================================================
	// members
	
	private final ChoreographerExecutor executor;
	private final List<ServiceQueryFormDTO> dependencyForms = new ArrayList<>();
	
	//-------------------------------------------------------------------------------------------------
	public ExecutorData(final ChoreographerExecutor executor, final List<ServiceQueryFormDTO> dependencyForms) {
		this.executor = executor;
		this.dependencyForms.addAll(dependencyForms);
	}

	//-------------------------------------------------------------------------------------------------
	public ChoreographerExecutor getExecutor() { return executor; }
	public List<ServiceQueryFormDTO> getDependencyForms() { return dependencyForms; }
}