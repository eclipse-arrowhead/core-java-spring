package eu.arrowhead.common.database.view.mscv;

import eu.arrowhead.common.dto.shared.mscv.OS;
import org.springframework.beans.factory.annotation.Value;

public interface TargetView {

    @Value("#{target.name}")
    String getName();

    @Value("#{target.os}")
    OS getOs();
}
