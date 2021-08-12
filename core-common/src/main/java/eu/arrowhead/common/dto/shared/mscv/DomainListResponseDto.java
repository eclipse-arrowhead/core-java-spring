package eu.arrowhead.common.dto.shared.mscv;

import java.util.List;

import org.springframework.data.domain.Page;

public class DomainListResponseDto extends PaginationResponse<DomainDto> {
    private static final long serialVersionUID = 1L;
    public DomainListResponseDto(final Page<DomainDto> page) {
        super(page);
    }

    public DomainListResponseDto(final List<DomainDto> content, final int currentPage, final int totalPages, final long totalElements) {
        super(content, currentPage, totalPages, totalElements);
    }
}
