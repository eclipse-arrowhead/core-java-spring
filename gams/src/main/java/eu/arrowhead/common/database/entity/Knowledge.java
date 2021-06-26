package eu.arrowhead.common.database.entity;

import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "gams_knowledge")
public class Knowledge extends AbstractEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "instanceId", referencedColumnName = "id", nullable = false)
    private GamsInstance instance;

    @Column(name = "key_", nullable = false, unique = true, length = 64)
    private String key;

    @Column(name = "value_", nullable = false, unique = false, length = 64)
    private String value;

    public Knowledge() {
        super();
    }

    public Knowledge(final GamsInstance instance, final String key, final String value) {
        super();
        this.instance = instance;
        this.key = key;
        this.value = value;
    }

    public GamsInstance getInstance() {
        return instance;
    }

    public void setInstance(final GamsInstance instance) {
        this.instance = instance;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Knowledge)) { return false; }
        final Knowledge knowledge = (Knowledge) o;
        return id == knowledge.id &&
                Objects.equals(instance, knowledge.instance) &&
                Objects.equals(key, knowledge.key) &&
                Objects.equals(value, knowledge.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, instance, key, value);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Knowledge.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("instance=" + instance.shortToString())
                .add("key='" + key + "'")
                .add("value='" + value + "'")
                .toString();
    }
}
