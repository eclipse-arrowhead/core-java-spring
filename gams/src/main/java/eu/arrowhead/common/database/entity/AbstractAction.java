package eu.arrowhead.common.database.entity;

import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.arrowhead.core.gams.dto.ActionType;

@Entity
@Table(name = "gams_action",
        uniqueConstraints = @UniqueConstraint(name = "u_action_name", columnNames = {"instanceId", "name"}))
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractAction extends NamedEntity {

    @Column(nullable = false, unique = false, updatable = false, length = 16)
    @Enumerated(EnumType.STRING)
    protected ActionType actionType;

    public AbstractAction() { super(); }

    public AbstractAction(final GamsInstance instance, final String name, final ActionType actionType) {
        super(instance, name);
        this.actionType = actionType;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(final ActionType actionType) {
        this.actionType = actionType;
    }

    public String shortToString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("instance=" + instance.shortToString())
                .add("name='" + name + "'")
                .add("type='" + actionType + "'")
                .toString();
    }
}
