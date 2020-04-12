package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.Layer;
import eu.arrowhead.core.msvc.database.OS;
import eu.arrowhead.core.msvc.database.entities.Mip;
import eu.arrowhead.core.msvc.database.entities.MipCategory;
import eu.arrowhead.core.msvc.database.entities.Script;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScriptRepository extends RefreshableRepository<Script, Long> {
    <S extends Script> Optional<S> findOneByMipAndOsAndLayer(final Mip mip, final OS os, final Layer layer);
}
