package eu.arrowhead.common.dto.shared.mscv;


import java.io.Serializable;
import java.util.StringJoiner;

public class TargetDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private OS os;

    public TargetDto() {
        super();
    }

    public TargetDto(final String name, final OS os) {
        this.name = name;
        this.os = os;
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


    @Override
    public String toString() {
        return new StringJoiner(", ", TargetDto.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("os=" + os)
                .toString();
    }
}
