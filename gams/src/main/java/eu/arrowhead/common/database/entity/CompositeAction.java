package eu.arrowhead.common.database.entity;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.common.collect.Lists;
import eu.arrowhead.core.gams.dto.ActionType;
import eu.arrowhead.core.gams.dto.CompositeType;

@Entity
@Table(name = "gams_composite_action")
@Inheritance(strategy = InheritanceType.JOINED)
public class CompositeAction extends AbstractAction {

    @Column(nullable = false, unique = false, length = 16)
    @Enumerated(EnumType.STRING)
    private CompositeType compositeType;

    @OneToMany(fetch = FetchType.EAGER)
    private List<ProcessableAction> actions;

    public CompositeAction() {
        super();
    }

    public CompositeAction(final GamsInstance instance, final String name, final CompositeType compositeType,
                           final List<ProcessableAction> actions) {
        super(instance, name, ActionType.COMPOSITE);
        this.compositeType = compositeType;
        this.actions = actions;
    }

    public CompositeAction(final GamsInstance instance, final String name, final CompositeType compositeType,
                           final ProcessableAction... actions) {
        super(instance, name, ActionType.COMPOSITE);
        this.compositeType = compositeType;
        this.actions = Lists.newArrayList(actions);
    }

    public CompositeType getCompositeType() {
        return compositeType;
    }

    public void setCompositeType(final CompositeType compositeType) {
        this.compositeType = compositeType;
    }

    public List<ProcessableAction> getActions() {
        return actions;
    }

    public void setActions(final List<ProcessableAction> actions) {
        this.actions = actions;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof CompositeAction)) { return false; }
        if (!super.equals(o)) { return false; }
        final CompositeAction that = (CompositeAction) o;
        return compositeType == that.compositeType &&
                Objects.equals(actions, that.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), compositeType, actions);
    }

    public String shortToString() {
        final StringJoiner sj = new StringJoiner(", ", CompositeAction.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("createdAt=" + createdAt)
                .add("name='" + name + "'")
                .add("compositeType=" + compositeType);

        final StringJoiner actionsSj = new StringJoiner(", ");
        for (AbstractAction action : actions) {
            actionsSj.add(action.toString());
        }
        return sj.add("actions=" + actions)
                 .toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CompositeAction.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("uid=" + uid)
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .add("instance=" + instance)
                .add("name='" + name + "'")
                .add("compositeType=" + compositeType)
                .add("actions=" + actions)
                .toString();
    }
}
