package eu.arrowhead.common.database.entity;

import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import eu.arrowhead.core.gams.dto.ActionType;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@Entity
@Table(name = "gams_logging_action")
public class LoggingAction extends AbstractAction {

    @Column(nullable = true)
    private String marker;

    public LoggingAction() {
        super();
    }

    public LoggingAction(final GamsInstance instance, final String name) {
        super(instance, name, ActionType.LOGGING);
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(final String marker) {
        this.marker = marker;
    }

    public Marker createMarker() {
        final Marker parent = MarkerManager.getMarker(getClass().getSimpleName());
        final Marker retValue;

        if (Objects.nonNull(this.marker)) {
            retValue = MarkerManager.getMarker(this.marker);
            retValue.addParents(parent);
        } else {
            retValue = parent;
        }

        return retValue;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof LoggingAction)) { return false; }
        if (!super.equals(o)) { return false; }
        final LoggingAction that = (LoggingAction) o;
        return Objects.equals(marker, that.marker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), marker);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("instance=" + instance)
                .add("marker='" + marker + "'")
                .toString();
    }
}
