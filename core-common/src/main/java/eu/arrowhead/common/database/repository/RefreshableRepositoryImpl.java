package eu.arrowhead.common.database.repository;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.Serializable;

@SuppressWarnings("squid:S00119")
public class RefreshableRepositoryImpl<T,ID extends Serializable> extends SimpleJpaRepository<T,ID> implements RefreshableRepository<T,ID> {
	
	//=================================================================================================
	// members

	private final EntityManager entityManager;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RefreshableRepositoryImpl(final JpaEntityInformation entityInformation, final EntityManager entityManager) {
		super(entityInformation, entityManager);
		this.entityManager = entityManager;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	@Transactional
	public void refresh(final T t) {
		entityManager.refresh(t);
	}
}