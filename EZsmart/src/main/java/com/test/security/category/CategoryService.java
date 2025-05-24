package com.test.security.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return new CategoryDTO(category);
    }

    public CategoryDTO createCategory(Category category) {
        Category savedCategory = categoryRepository.save(category);
        return new CategoryDTO(savedCategory);
    }

    @Transactional
    public Optional<CategoryDTO> updateCategory(Long id, Category categoryDetails) {
        return categoryRepository.findById(id)
                .map(existingCategory -> {
                    existingCategory.setName(categoryDetails.getName());
                    Category updatedCategory = categoryRepository.save(existingCategory);
                    return new CategoryDTO(updatedCategory);
                });
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}