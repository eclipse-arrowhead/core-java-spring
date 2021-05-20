package eu.arrowhead.core.gams.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GamsInstanceListDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<GamsInstanceDto> data = new ArrayList<>();
    private long count;

    public GamsInstanceListDto() { super(); }

    public GamsInstanceListDto(final List<GamsInstanceDto> data, final long count) {
        this.data = data;
        this.count = count;
    }

    public List<GamsInstanceDto> getData() {
        return data;
    }

    public long getCount() {
        return count;
    }

    public void setData(final List<GamsInstanceDto> data) {
        this.data = data;
    }

    public void setCount(final long count) {
        this.count = count;
    }
}
