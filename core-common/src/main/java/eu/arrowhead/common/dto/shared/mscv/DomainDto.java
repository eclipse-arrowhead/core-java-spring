package eu.arrowhead.common.dto.shared.mscv;


import java.io.Serializable;
import java.util.StringJoiner;

public class DomainDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;

    public DomainDto() {
        super();
    }

    public DomainDto(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DomainDto.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .toString();
    }
}
