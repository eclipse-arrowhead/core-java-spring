package eu.arrowhead.common.dto.shared.mscv;

import java.util.List;

import org.springframework.data.domain.Page;

public class ScriptListResponseDto extends PaginationResponse<ScriptResponseDto> {
    private static final long serialVersionUID = 1L;
    public ScriptListResponseDto(final Page<ScriptResponseDto> page) {
        super(page);
    }

    public ScriptListResponseDto(final List<ScriptResponseDto> content, final int currentPage, final int totalPages, final long totalElements) {
        super(content, currentPage, totalPages, totalElements);
    }
}
