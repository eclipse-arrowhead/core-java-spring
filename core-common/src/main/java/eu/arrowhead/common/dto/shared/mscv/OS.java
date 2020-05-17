package eu.arrowhead.common.dto.shared.mscv;

public enum OS {
    WINDOWS, LINUX, MAC_OS;

    public String path() {
        return name().toLowerCase();
    }
}
