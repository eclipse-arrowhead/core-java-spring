package eu.arrowhead.common.dto.shared.mscv;


import java.io.Serializable;
import java.util.StringJoiner;

public class ScriptRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private MipIdentifierDto mip;
    private Layer layer;
    private OS os;

    public ScriptRequestDto() {
        super();
    }

    public ScriptRequestDto(final MipIdentifierDto mip, final Layer layer, final OS os) {
        this.mip = mip;
        this.layer = layer;
        this.os = os;
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

    public MipIdentifierDto getMip() {
        return mip;
    }

    public void setMip(final MipIdentifierDto mip) {
        this.mip = mip;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ScriptRequestDto.class.getSimpleName() + "[", "]")
                .add("mip=" + mip)
                .add("layer='" + layer + "'")
                .add("os='" + os + "'")
                .toString();
    }
}
