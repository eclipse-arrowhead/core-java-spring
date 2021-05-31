package eu.arrowhead.common.database.repository.mscv;

import java.util.Optional;
import java.util.Set;

import eu.arrowhead.common.database.entity.mscv.Mip;
import eu.arrowhead.common.database.entity.mscv.Script;
import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.common.dto.shared.mscv.OS;
import org.springframework.stereotype.Repository;

@Repository
public interface ScriptRepository extends RefreshableRepository<Script, Long> {

    <S extends Script> Optional<S> findOneByMipAndLayerAndOs(final Mip mip, final Layer layer, final OS os);

    <S extends Script> Set<S> findAllByLayer(final Layer layer);

    <S extends Script> Set<S> findAllByOs(final OS os);
}
