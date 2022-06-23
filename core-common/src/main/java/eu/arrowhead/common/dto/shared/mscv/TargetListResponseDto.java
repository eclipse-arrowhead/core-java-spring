package eu.arrowhead.common.dto.shared.mscv;

import java.util.List;

import org.springframework.data.domain.Page;

public class TargetListResponseDto extends PaginationResponse<SshTargetDto> {
    private static final long serialVersionUID = 1L;
    public TargetListResponseDto(final Page<SshTargetDto> page) {
        super(page);
    }

    public TargetListResponseDto(final List<SshTargetDto> content, final int currentPage, final int totalPages, final long totalElements) {
        super(content, currentPage, totalPages, totalElements);
    }
}
