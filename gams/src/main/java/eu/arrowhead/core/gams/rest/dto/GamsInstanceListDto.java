package eu.arrowhead.core.gams.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GamsInstanceListDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<GamsInstanceDto> data = new ArrayList<>();
    private int count;
    private int currentPage;
    private int totalPages;

    public GamsInstanceListDto() { super(); }

    public GamsInstanceListDto(final List<GamsInstanceDto> data, final int count) {
        this(data, count, 1, 1);
    }

    public GamsInstanceListDto(final List<GamsInstanceDto> data, final int count, final int currentPage, final int totalPages) {
        super();
        this.data = data;
        this.count = count;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    public List<GamsInstanceDto> getData() {
        return data;
    }

    public void setData(final List<GamsInstanceDto> data) {
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
