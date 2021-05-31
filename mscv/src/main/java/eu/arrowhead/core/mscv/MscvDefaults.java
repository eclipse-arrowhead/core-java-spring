package eu.arrowhead.core.mscv;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import eu.arrowhead.common.dto.shared.mscv.OS;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConfigurationProperties("mscv")
// @ConstructorBinding would allow using final fields. needs Spring Boot 2.2
public class MscvDefaults {

    @NotBlank
    private String listName = "default list";

    @Min(1)
    @Max(100)
    private Short mipWeight = 100;

    // it is expected that a verification takes a few minutes
    @Min(5)
    private Long verificationInterval = TimeUnit.HOURS.toMinutes(6L); // minutes

    @NotNull
    private OS os = OS.LINUX;

    @NotNull
    private SshDefaults ssh;

    @NotBlank
    private String defaultPath = Path.of(".").toAbsolutePath().toString();

    public MscvDefaults() { super(); }

    @PostConstruct
    public void createDefaults() {
        ssh.createDefaults();
    }

    public String getListName() {
        return listName;
    }

    public void setListName(final String listName) {
        this.listName = listName;
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

    public String getDefaultPath() {
        return defaultPath;
    }

    public void setDefaultPath(final String defaultPath) {
        this.defaultPath = defaultPath;
    }

    public static class SshDefaults {

        private static final String DEFAULT_SSH_DIR = ".ssh";
        private static final String DEFAULT_SSH_PRIVATE_KEY = "mscv_rsa";
        private static final String DEFAULT_SSH_PUBLIC_KEY = "mscv_rsa.pub";
        private static final String DEFAULT_PRIVATE_KEY_PASSWORD = "mscv-password";

        @Min(1)
        @Max(65535)
        private Integer port = 22;

        @Min(1)
        @Max(180)
        private Long connectTimeout = 30L; // seconds

        @Min(1)
        @Max(180)
        private Long authTimeout = 30L; // seconds

        private String privateKeyFile = null;
        private String publicKeyFile = null;
        private String keyPairFile = null;
        private String privateKeyPassword = null;
        private boolean emptyPassword = false;

        public boolean hasKeyPairFile() {
            return Objects.nonNull(keyPairFile) && Path.of(keyPairFile).toFile().exists();
        }

        public boolean hasPrivateKeyFile() {
            return Objects.nonNull(privateKeyFile) && Path.of(privateKeyFile).toFile().exists();
        }

        public boolean hasPublicKeyFile() {
            return Objects.nonNull(publicKeyFile) && Path.of(publicKeyFile).toFile().exists();
        }

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

        public String getPrivateKeyFile() {
            return privateKeyFile;
        }

        public void setPrivateKeyFile(final String privateKeyFile) {
            this.privateKeyFile = privateKeyFile;
        }

        public String getPublicKeyFile() {
            return publicKeyFile;
        }

        public void setPublicKeyFile(final String publicKeyFile) {
            this.publicKeyFile = publicKeyFile;
        }

        public String getKeyPairFile() {
            return keyPairFile;
        }

        public void setKeyPairFile(final String keyPairFile) {
            this.keyPairFile = keyPairFile;
        }

        public String getPrivateKeyPassword() {
            return privateKeyPassword;
        }

        public void setPrivateKeyPassword(final String privateKeyPassword) {
            this.privateKeyPassword = privateKeyPassword;
        }

        public boolean isEmptyPassword() {
            return emptyPassword;
        }

        public void setEmptyPassword(final boolean emptyPassword) {
            this.emptyPassword = emptyPassword;
        }

        private void createDefaults() {
            final String userHome = SystemUtils.USER_HOME;
            if (!StringUtils.hasText(privateKeyPassword)) {
                this.privateKeyPassword = SshDefaults.DEFAULT_PRIVATE_KEY_PASSWORD;
            }
            if (!StringUtils.hasText(privateKeyFile)) {
                this.privateKeyFile = Path.of(userHome, DEFAULT_SSH_DIR, DEFAULT_SSH_PRIVATE_KEY).toString();
            }
            if (!StringUtils.hasText(publicKeyFile)) {
                this.publicKeyFile = Path.of(userHome, DEFAULT_SSH_DIR, DEFAULT_SSH_PUBLIC_KEY).toString();
            }
        }
    }
}
