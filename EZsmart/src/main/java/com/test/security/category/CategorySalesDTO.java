package com.test.security.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySalesDTO {
    private Long categoryId;
    private String categoryName;
    private Long totalItemsSold;
    private Double totalRevenue;
    private String categoryImage;
} 