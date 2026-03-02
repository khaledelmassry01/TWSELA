package com.twsela.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Paginated API response wrapper.
 *
 * @param <T> the element type of the page content
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiPageResponse<T> extends ApiResponse<List<T>> {

    private int page;
    private int size;
    private int totalPages;
    private long totalElements;

    public ApiPageResponse() { super(); }

    public ApiPageResponse(List<T> content, int page, int size, int totalPages, long totalElements) {
        super(true, null, content, null);
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public ApiPageResponse(List<T> content, String message, int page, int size, int totalPages, long totalElements) {
        super(true, message, content, null);
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    /** Convenience factory from a Spring Data Page. */
    public static <T> ApiPageResponse<T> of(org.springframework.data.domain.Page<T> springPage) {
        return new ApiPageResponse<>(
            springPage.getContent(),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalPages(),
            springPage.getTotalElements()
        );
    }

    public static <T> ApiPageResponse<T> of(org.springframework.data.domain.Page<T> springPage, String message) {
        return new ApiPageResponse<>(
            springPage.getContent(),
            message,
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalPages(),
            springPage.getTotalElements()
        );
    }

    /** Factory for a simple list (no pagination metadata). */
    public static <T> ApiPageResponse<T> ofList(List<T> items, String message) {
        return new ApiPageResponse<>(items, message, 0, items.size(), 1, items.size());
    }

    // ── Getters & Setters ────────────────────────────────────────

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
}
