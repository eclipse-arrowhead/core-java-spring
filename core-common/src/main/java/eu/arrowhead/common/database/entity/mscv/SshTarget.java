package eu.arrowhead.common.database.entity.mscv;

import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.arrowhead.common.dto.shared.mscv.OS;

@Entity
@Table(name = "mscv_ssh_target",
        uniqueConstraints = @UniqueConstraint(name = "u_address_port", columnNames = {"address", "port"}))
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
public class SshTarget extends Target {

    @Column(nullable = false, length = 32)
    private String address;

    @Column(nullable = false)
    private Integer port;

    @Column(nullable = true)
    private String authInfo;

    public SshTarget() { super(); }

    public SshTarget(final String name, final OS os, final String address, final Integer port) {
        super(name, os);
        this.address = address;
        this.port = port;
    }

    public SshTarget(final Long id, final String name, final OS os, final String address, final Integer port) {
        super(id, name, os);
        this.address = address;
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(final Integer port) {
        this.port = port;
    }

    public String getAuthInfo() {
        return authInfo;
    }

    public void setAuthInfo(final String authInfo) {
        this.authInfo = authInfo;
    }

    @Override
    protected void appendToString(final StringJoiner sj) {
        sj.add("address=" + address)
          .add("port='" + port + "'");
    }
}
