package eu.arrowhead.common.dto.shared.mscv;


import java.util.StringJoiner;

public class SshTargetDto extends TargetDto {

    private static final long serialVersionUID = 1L;
    private String address;
    private Integer port;

    public SshTargetDto() {
        super();
    }

    public SshTargetDto(final String name, final OS os, final String address, final Integer port) {
        super(name, os);
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

    @Override
    public String toString() {
        return new StringJoiner(", ", SshTargetDto.class.getSimpleName() + "[", "]")
                .add("name='" + getName() + "'")
                .add("os=" + getOs())
                .add("address='" + address + "'")
                .add("port=" + port)
                .toString();
    }
}
