package eu.arrowhead.common.dto.shared.mscv;


import java.util.StringJoiner;

public class SshTargetDto {

    private String name;
    private OS os;
    private String address;
    private long port;

    public SshTargetDto() {
        super();
    }

    public SshTargetDto(final String name, final OS os, final String address, final long port) {
        this.name = name;
        this.os = os;
        this.address = address;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public OS getOs() {
        return os;
    }

    public void setOs(final OS os) {
        this.os = os;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public long getPort() {
        return port;
    }

    public void setPort(long port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SshTargetDto.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("os=" + os)
                .add("address='" + address + "'")
                .add("port=" + port)
                .toString();
    }
}
