package eu.arrowhead.common.dto.shared.mscv;

public enum Layer {
    DEVICE, SYSTEM, SERVICE;

    public String path() {
        return name().toLowerCase();
    }
}
