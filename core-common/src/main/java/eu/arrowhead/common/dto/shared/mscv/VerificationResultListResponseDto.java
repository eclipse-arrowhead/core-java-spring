package eu.arrowhead.common.dto.shared.mscv;

import java.util.List;

import org.springframework.data.domain.Page;

public class VerificationResultListResponseDto extends PaginationResponse<VerificationResultDto> {
    private static final long serialVersionUID = 1L;
    public VerificationResultListResponseDto(final Page<VerificationResultDto> page) {
        super(page);
    }

    public VerificationResultListResponseDto(final List<VerificationResultDto> content, final int currentPage, final int totalPages, final long totalElements) {
        super(content, currentPage, totalPages, totalElements);
    }
}
