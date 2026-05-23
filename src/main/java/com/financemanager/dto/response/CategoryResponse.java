package com.financemanager.dto.response;

import com.financemanager.entity.Category.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private CategoryType type;
    private boolean isCustom;
}
