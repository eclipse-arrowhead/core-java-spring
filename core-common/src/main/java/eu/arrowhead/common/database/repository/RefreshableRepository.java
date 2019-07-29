package eu.arrowhead.common.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@SuppressWarnings("squid:S00119")
@NoRepositoryBean
public interface RefreshableRepository<T,ID extends Serializable> extends JpaRepository<T,ID> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public void refresh(final T t);
}