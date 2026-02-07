package com.joseguillard.my_blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private int totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    /**
     * Builds paged response from Spring Data `Page`
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        // Builds paginated response from the Spring Data page
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getNumberOfElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
