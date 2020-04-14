package eu.arrowhead.common.database.view.mscv;

import org.springframework.beans.factory.annotation.Value;

public interface SshTargetView extends TargetView {


    @Value("#{target.address}")
    String getAddress();

    @Value("#{target.port}")
    Integer getPort();
}
