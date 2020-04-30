package eu.arrowhead.core.mscv;

import java.util.concurrent.TimeUnit;

import eu.arrowhead.common.dto.shared.mscv.OS;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("mscv.defaults")
// @ConstructorBinding would allow using final fields. needs Spring Boot 2.2
public class MscvDefaults {

    private String defaultList = "default list";
    private Short mipWeight = 100;
    private Long verificationInterval = TimeUnit.HOURS.toMinutes(6L); // minutes
    private OS os = OS.LINUX;

    private SshDefaults ssh;

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

    public Long getVerificationInterval() {
        return verificationInterval;
    }

    public void setVerificationInterval(final Long verificationInterval) {
        this.verificationInterval = verificationInterval;
    }

    public OS getOs() {
        return os;
    }

    public void setOs(final OS os) {
        this.os = os;
    }

    public SshDefaults getSsh() {
        return ssh;
    }

    public void setSsh(final SshDefaults ssh) {
        this.ssh = ssh;
    }

    public static class SshDefaults {
        private Integer port = 22;
        private Long connectTimeout = 10L; // seconds
        private Long authTimeout = 10L; // seconds

        public Integer getPort() {
            return port;
        }

        public void setPort(final Integer port) {
            this.port = port;
        }

        public Long getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(final Long connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Long getAuthTimeout() {
            return authTimeout;
        }

        public void setAuthTimeout(final Long authTimeout) {
            this.authTimeout = authTimeout;
        }
    }
}
