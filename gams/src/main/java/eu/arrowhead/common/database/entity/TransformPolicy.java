package eu.arrowhead.common.database.entity;

import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "gams_transform_policy")
public class TransformPolicy extends AbstractPolicy {

    @Column(nullable = false, unique = false, length = 64)
    private String expression;

    @Column(nullable = false, unique = false, length = 16)
    private String variable;

    public TransformPolicy() { super(); }

    public String getExpression() {
        return expression;
    }

    public void setExpression(final String expression) {
        this.expression = expression;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(final String variable) {
        this.variable = variable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof TransformPolicy)) { return false; }
        if (!super.equals(o)) { return false; }
        final TransformPolicy that = (TransformPolicy) o;
        return Objects.equals(expression, that.expression) &&
                Objects.equals(variable, that.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expression, variable);
    }


    public String shortToString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("sensor=" + sensor.shortToString())
                .add("expression='" + expression + "'")
                .toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("sensor=" + sensor)
                .add("expression='" + expression + "'")
                .add("id=" + id)
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .add("uid=" + uid)
                .toString();
    }
}
