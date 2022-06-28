package eu.arrowhead.core.gams.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorListDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<SensorDto> data = new ArrayList<>();
    private int count;
    private int currentPage;
    private int totalPages;

    public SensorListDto() { super(); }

    public SensorListDto(final List<SensorDto> data, final int count) {
        this(data, count, 1, 1);
    }

    public SensorListDto(final List<SensorDto> data, final int count, final int currentPage, final int totalPages) {
        super();
        this.data = data;
        this.count = count;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    public List<SensorDto> getData() {
        return data;
    }

    public void setData(final List<SensorDto> data) {
        this.data = data;
    }

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(final int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(final int totalPages) {
        this.totalPages = totalPages;
    }
}
