package eu.arrowhead.common.database.repository.mscv;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.common.dto.shared.mscv.OS;
import eu.arrowhead.common.database.entity.mscv.Mip;
import eu.arrowhead.common.database.entity.mscv.Script;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScriptRepository extends RefreshableRepository<Script, Long> {
    <S extends Script> Optional<S> findOneByMipAndOsAndLayer(final Mip mip, final OS os, final Layer layer);
}
