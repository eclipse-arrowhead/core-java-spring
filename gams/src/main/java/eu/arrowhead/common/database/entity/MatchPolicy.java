package eu.arrowhead.common.database.entity;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import eu.arrowhead.core.gams.dto.MatchType;
import eu.arrowhead.core.gams.dto.PolicyType;

@Entity
@Table(name = "gams_match_policy")
public class MatchPolicy extends AbstractPolicy {

    @Column(name = "matchType", nullable = false, unique = false, length = 16)
    @Enumerated(EnumType.STRING)
    private MatchType matchType;

    @Column(nullable = false, unique = false, length = 16)
    private Long number;

    public MatchPolicy() { super(); }

    public MatchPolicy(final Sensor sensor, final String sourceKnowledge, final String targetKnowledge,
                       final MatchType matchType, final Long number) {
        super(sensor, sourceKnowledge, targetKnowledge, PolicyType.MATCH);
        this.matchType = matchType;
        this.number = number;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(final Long number) {
        this.number = number;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(final MatchType matchType) {
        this.matchType = matchType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof MatchPolicy)) { return false; }
        if (!super.equals(o)) { return false; }
        final MatchPolicy that = (MatchPolicy) o;
        return matchType == that.matchType &&
                Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), matchType, number);
    }
}
