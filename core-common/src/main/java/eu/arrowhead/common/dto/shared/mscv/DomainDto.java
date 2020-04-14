package eu.arrowhead.common.dto.shared.mscv;


import java.util.StringJoiner;

public class DomainDto {

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
