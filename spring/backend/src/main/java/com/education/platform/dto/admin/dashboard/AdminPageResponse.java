package com.education.platform.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPageResponse<T> {

    private List<T> items;
    private long total;
    private int page;
    private int size;
    private int totalPages;
}
