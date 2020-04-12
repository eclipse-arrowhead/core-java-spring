package eu.arrowhead.core.msvc.database.view;

import eu.arrowhead.core.msvc.database.OS;
import org.springframework.beans.factory.annotation.Value;

public interface TargetView {

    @Value("#{target.name}")
    String getName();

    @Value("#{target.os}")
    OS getOs();
}
