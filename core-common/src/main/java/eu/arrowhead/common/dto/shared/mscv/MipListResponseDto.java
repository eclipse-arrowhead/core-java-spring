package eu.arrowhead.common.dto.shared.mscv;

import java.util.List;

import org.springframework.data.domain.Page;

public class MipListResponseDto extends PaginationResponse<MipDto> {
    public MipListResponseDto(final Page<MipDto> page) {
        super(page);
    }

    public MipListResponseDto(final List<MipDto> content, final int currentPage, final int totalPages, final long totalElements) {
        super(content, currentPage, totalPages, totalElements);
    }
}
