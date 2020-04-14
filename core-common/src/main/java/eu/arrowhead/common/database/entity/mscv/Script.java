package eu.arrowhead.common.database.entity.mscv;


import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.common.dto.shared.mscv.OS;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "mscv_script",
        uniqueConstraints = @UniqueConstraint(name = "u_script_mip_layer", columnNames = {"mipId", "os", "layer"}))
public class Script {

    @Id
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mipId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_script_mip", value = ConstraintMode.CONSTRAINT))
    private Mip mip;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private Layer layer;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private OS os;

    @Column(nullable = false)
    private String path;

    public Script() { super(); }

    public Script(final Mip mip, final Layer layer, final OS os, final String path) {
        this.mip = mip;
        this.layer = layer;
        this.os = os;
        this.path = path;
    }

    public Script(final Long id, final Mip mip, final Layer layer, final OS os, final String path) {
        this.id = id;
        this.mip = mip;
        this.layer = layer;
        this.os = os;
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Mip getMip() {
        return mip;
    }

    public void setMip(final Mip mip) {
        this.mip = mip;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(final Layer layer) {
        this.layer = layer;
    }

    public OS getOs() {
        return os;
    }

    public void setOs(final OS os) {
        this.os = os;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final Script script = (Script) o;
        return Objects.equals(id, script.id) &&
                Objects.equals(mip, script.mip) &&
                layer == script.layer &&
                os == script.os &&
                Objects.equals(path, script.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mip, layer, os, path);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Script.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("mip=" + mip)
                .add("layer=" + layer)
                .add("os=" + os)
                .add("path=" + path)
                .toString();
    }
}
