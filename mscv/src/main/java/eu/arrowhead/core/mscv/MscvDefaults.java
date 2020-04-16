package eu.arrowhead.core.mscv;

import eu.arrowhead.common.dto.shared.mscv.OS;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("mscv.defaults")
public class MscvDefaults {

    private String defaultList = "default";
    private Short mipWeight = 100;
    private Integer verificationInterval = 3600;
    private OS os = OS.LINUX;
    private Integer sshPort = 22;

    public MscvDefaults() { super(); }

    public String getDefaultList() {
        return defaultList;
    }

    public void setDefaultList(final String defaultList) {
        this.defaultList = defaultList;
    }

    public Short getMipWeight() {
        return mipWeight;
    }

    public void setMipWeight(final Short mipWeight) {
        this.mipWeight = mipWeight;
    }

    public Integer getVerificationInterval() {
        return verificationInterval;
    }

    public void setVerificationInterval(final Integer verificationInterval) {
        this.verificationInterval = verificationInterval;
    }

    public OS getOs() {
        return os;
    }

    public void setOs(final OS os) {
        this.os = os;
    }

    public Integer getSshPort() {
        return sshPort;
    }

    public void setSshPort(final Integer sshPort) {
        this.sshPort = sshPort;
    }
}
