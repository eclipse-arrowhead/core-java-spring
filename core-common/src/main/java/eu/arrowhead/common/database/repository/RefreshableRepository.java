package eu.arrowhead.common.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface RefreshableRepository<T,ID extends Serializable> extends JpaRepository<T,ID> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	void refresh(final T t);
}