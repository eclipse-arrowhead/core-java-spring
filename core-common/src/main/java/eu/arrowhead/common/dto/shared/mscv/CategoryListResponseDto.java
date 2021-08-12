package eu.arrowhead.common.dto.shared.mscv;

import java.util.List;

import org.springframework.data.domain.Page;

public class CategoryListResponseDto extends PaginationResponse<CategoryDto> {
    private static final long serialVersionUID = 1L;
    public CategoryListResponseDto(final Page<CategoryDto> page) {
        super(page);
    }

    public CategoryListResponseDto(final List<CategoryDto> content, final int currentPage, final int totalPages, final long totalElements) {
        super(content, currentPage, totalPages, totalElements);
    }
}
