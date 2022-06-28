package eu.arrowhead.common.dto.shared.mscv;

import java.io.Serializable;
import java.util.List;
import java.util.StringJoiner;

import org.springframework.data.domain.Page;
import org.springframework.util.Assert;

public abstract class PaginationResponse<P> implements Serializable {

    private static final long serialVersionUID = 1L;
    private final List<P> content;
    private final int currentPage;
    private final int totalPages;
    private final int currentElements;
    private final long totalElements;
    private final boolean hasNext;

    public PaginationResponse(final Page<P> page) {
        Assert.notNull(page, "Page must not be null");
        content = List.copyOf(page.getContent());
        currentPage = page.getNumber();
        currentElements = page.getNumberOfElements();
        totalPages = page.getTotalPages();
        totalElements = page.getTotalElements();
        hasNext = page.hasNext();
    }

    public PaginationResponse(final List<P> content, final int currentPage,
                              final int totalPages, final long totalElements) {
        Assert.notNull(content, "Collection must not be null");
        this.content = List.copyOf(content);
        this.currentPage = currentPage;
        this.currentElements = content.size();
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.hasNext = totalPages > (currentPage + 1);
    }

    public List<P> getContent() {
        return content;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getCurrentElements() {
        return currentElements;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("currentPage=" + currentPage)
                .add("totalPages=" + totalPages)
                .add("currentElements=" + currentElements)
                .add("totalElements=" + totalElements)
                .add("hasNext=" + hasNext)
                .toString();
    }
}
