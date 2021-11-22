package eu.arrowhead.common.database.repository;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.boot.logging.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Logs;

@Repository
public interface LogsRepository extends RefreshableRepository<Logs,String> {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Page<Logs> findAllBySystemAndLogLevelInAndEntryDateBetween(final CoreSystem system, final List<LogLevel> levels, final ZonedDateTime from, final ZonedDateTime to, final Pageable pageable);
	public Page<Logs> findAllBySystemAndLogLevelInAndEntryDateBetweenAndLoggerContainsIgnoreCase(final CoreSystem system, final List<LogLevel> levels, final ZonedDateTime from, final ZonedDateTime to, final String logger, final Pageable pageable);
}