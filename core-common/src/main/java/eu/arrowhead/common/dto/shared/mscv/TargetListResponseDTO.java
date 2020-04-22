package eu.arrowhead.common.dto.shared.mscv;

import org.springframework.data.domain.Page;

import java.util.List;

public class TargetListResponseDTO extends PaginationResponse<SshTargetDto> {
    public TargetListResponseDTO(final Page<SshTargetDto> page) {
        super(page);
    }

    public TargetListResponseDTO(final List<SshTargetDto> content, final int currentPage, final int totalPages, final long totalElements) {
        super(content, currentPage, totalPages, totalElements);
    }
}
