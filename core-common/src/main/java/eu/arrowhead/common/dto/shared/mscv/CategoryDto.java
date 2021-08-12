package eu.arrowhead.common.dto.shared.mscv;


import java.io.Serializable;
import java.util.StringJoiner;

public class CategoryDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private String abbreviation;

    public CategoryDto() {
        super();
    }

    public CategoryDto(final String name, final String abbreviation) {
        this.name = name;
        this.abbreviation = abbreviation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

  @Override
  public String toString() {
    return new StringJoiner(", ", CategoryDto.class.getSimpleName() + "[", "]")
            .add("name='" + name + "'")
            .add("abbreviation='" + abbreviation + "'")
            .toString();
  }
}
