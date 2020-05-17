package eu.arrowhead.common.dto.shared.mscv;


import java.io.Serializable;
import java.util.StringJoiner;

import eu.arrowhead.common.database.view.mscv.MipView;

public class ScriptResponseDto implements Serializable {

    private MipView mip;
    private Layer layer;
    private OS os;
    private String contentUri;

    public ScriptResponseDto() {
        super();
    }

    public ScriptResponseDto(final MipView mip, final Layer layer, final OS os, final String contentUri) {
        this.mip = mip;
        this.layer = layer;
        this.os = os;
        this.contentUri = contentUri;
    }

    public MipView getMip() {
        return mip;
    }

    public void setMip(final MipView mip) {
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
