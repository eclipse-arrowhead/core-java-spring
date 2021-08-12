package eu.arrowhead.common.dto.shared.mscv;


import java.io.Serializable;
import java.util.StringJoiner;

public class ScriptResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private MipDto mip;
    private Layer layer;
    private OS os;
    private String contentUri;

    public ScriptResponseDto() {
        super();
    }

    public ScriptResponseDto(final MipDto mip, final Layer layer, final OS os, final String contentUri) {
        this.mip = mip;
        this.layer = layer;
        this.os = os;
        this.contentUri = contentUri;
    }

    public MipDto getMip() {
        return mip;
    }

    public void setMip(final MipDto mip) {
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

    public String getContentUri() {
        return contentUri;
    }

    public void setContentUri(String contentUri) {
        this.contentUri = contentUri;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", ScriptResponseDto.class.getSimpleName() + "[", "]")
                .add("mip=" + mip)
                .add("layer='" + layer + "'")
                .add("os='" + os + "'")
                .add("uri='" + contentUri + "'")
                .toString();
    }
}
