package eu.arrowhead.common.dto.shared.mscv;

import java.util.List;

import org.springframework.data.domain.Page;

public class StandardListResponseDto extends PaginationResponse<StandardDto> {
    private static final long serialVersionUID = 1L;
    public StandardListResponseDto(final Page<StandardDto> page) {
        super(page);
    }

    public StandardListResponseDto(final List<StandardDto> content, final int currentPage, final int totalPages, final long totalElements) {
        super(content, currentPage, totalPages, totalElements);
    }
}
