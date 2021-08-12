package eu.arrowhead.common.dto.shared.mscv;

import java.util.List;

import org.springframework.data.domain.Page;

public class MipListResponseDto extends PaginationResponse<MipDto> {
    private static final long serialVersionUID = 1L;
    public MipListResponseDto(final Page<MipDto> page) {
        super(page);
    }

    public MipListResponseDto(final List<MipDto> content, final int currentPage, final int totalPages, final long totalElements) {
        super(content, currentPage, totalPages, totalElements);
    }
}
