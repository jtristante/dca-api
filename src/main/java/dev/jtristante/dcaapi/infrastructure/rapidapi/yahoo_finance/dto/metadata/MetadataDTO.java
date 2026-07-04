package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.metadata;

public abstract class MetadataDTO {

    protected final String version;
    protected final int status;

    protected MetadataDTO(String version, int status) {
        this.version = version;
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public int getStatus() {
        return status;
    }
}
