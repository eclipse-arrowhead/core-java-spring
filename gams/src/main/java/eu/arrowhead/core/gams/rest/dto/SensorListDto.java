package eu.arrowhead.core.gams.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorListDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<SensorDto> data = new ArrayList<>();
    private long count;

    public SensorListDto() { super(); }

    public SensorListDto(final List<SensorDto> data, final long count) {
        this.data = data;
        this.count = count;
    }

    public List<SensorDto> getData() {
        return data;
    }

    public long getCount() {
        return count;
    }

    public void setData(final List<SensorDto> data) {
        this.data = data;
    }

    public void setCount(final long count) {
        this.count = count;
    }
}
