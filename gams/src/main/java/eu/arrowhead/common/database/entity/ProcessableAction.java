package eu.arrowhead.common.database.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import eu.arrowhead.common.database.StringMapConverter;
import eu.arrowhead.common.database.StringSetConverter;
import eu.arrowhead.core.gams.dto.ActionType;


@Entity
@Table(name = "gams_processable_action")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ProcessableAction extends AbstractAction {

    // input - list of variable names
    @Column(nullable = true, unique = false, length = 128)
    @Convert(converter = StringSetConverter.class)
    protected Set<String> knowledgeKeys = new HashSet<>();

    // output - key: variable name, value: regex to parse the result
    @Convert(converter = StringMapConverter.class)
    @Column(nullable = true, unique = false, length = 512)
    protected Map<String, String> processors = new HashMap<>();

    public ProcessableAction() {
        super();
    }

    public ProcessableAction(final GamsInstance instance, final String name, final ActionType actionType, final String... knowledgeKeys) {
        super(instance, name, actionType);
        this.knowledgeKeys = Set.of(knowledgeKeys);
    }

    public Set<String> getKnowledgeKeys() {
        return knowledgeKeys;
    }

    public void setKnowledgeKeys(final Set<String> knowledgeKeys) {
        this.knowledgeKeys = knowledgeKeys;
    }

    public Map<String, String> getProcessors() {
        return processors;
    }

    public void setProcessors(final Map<String, String> processors) {
        this.processors = processors;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("uid=" + uid)
                .add("instance=" + instance.shortToString())
                .add("name='" + name + "'")
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .toString();
    }
}
