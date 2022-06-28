package eu.arrowhead.common.database.entity;

import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "gams_action_plan")
public class ActionPlan extends NamedEntity {

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private AbstractAction action;

    public ActionPlan() {}

    public ActionPlan(final GamsInstance instance, final String name, final AbstractAction action) {
        this.instance = instance;
        this.name = name;
        this.action = action;
    }

    public GamsInstance getInstance() {
        return instance;
    }

    public void setInstance(final GamsInstance instance) {
        this.instance = instance;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public AbstractAction getAction() {
        return action;
    }

    public void setAction(final AbstractAction action) {
        this.action = action;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof ActionPlan)) { return false; }
        if (!super.equals(o)) { return false; }
        final ActionPlan that = (ActionPlan) o;
        return Objects.equals(instance, that.instance) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instance, name);
    }

    public String shortToString() {
        final StringJoiner sj = new StringJoiner(", ", ActionPlan.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("instance=" + instance.shortToString())
                .add("instance=" + action.shortToString());
        return sj.toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ActionPlan.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("createdAt=" + createdAt)
                .add("name='" + name + "'")
                .add("instance=" + instance.shortToString())
                .add("uid=" + uid)
                .add("action=" + action)
                .toString();
    }
}