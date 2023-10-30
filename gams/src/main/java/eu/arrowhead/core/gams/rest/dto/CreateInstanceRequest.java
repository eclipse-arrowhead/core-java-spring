package eu.arrowhead.core.gams.rest.dto;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "request")
public class CreateInstanceRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(required = true, value = "The name of the new instance", dataType = "string")
    private String name;
    @ApiModelProperty(required = true, value = "Delay the trigger of gams loops in order to gather more events", dataType = "long", example = "5")
    private Long delayInSeconds;
    @ApiModelProperty(required = false, value = "The name of maintainer", dataType = "string")
    private String owner;
    @ApiModelProperty(required = false, value = "Contact information (e-mail) of the maintainer", dataType = "string")
    private String email;

    public CreateInstanceRequest() {
        super();
    }

    public CreateInstanceRequest(final String name, final Long delayInSeconds) {
        this.name = name;
        this.delayInSeconds = delayInSeconds;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getDelayInSeconds() {
        return delayInSeconds;
    }

    public void setDelayInSeconds(final Long delayInSeconds) {
        this.delayInSeconds = delayInSeconds;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final CreateInstanceRequest that = (CreateInstanceRequest) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(delayInSeconds, that.delayInSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, owner, delayInSeconds);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CreateInstanceRequest.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("delay=" + delayInSeconds)
                .add("owner=" + owner)
                .toString();
    }
}
