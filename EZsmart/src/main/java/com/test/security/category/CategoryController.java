package com.test.security.category;
import com.test.security.seller.Seller;
import com.test.security.user.User;
import com.test.security.user.UserRepository;
import com.test.security.product.Product;
import com.test.security.products.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryService.createCategory(category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestBody Category categoryDetails) {
        if(categoryService.getCategoryById(id) == null)
            throw  new RuntimeException("Category not found");
        categoryService.updateCategory(id, categoryDetails);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if(categoryService.getCategoryById(id) == null)
            throw new RuntimeException("Category not found");
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/seller/top-categories")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public List<CategorySalesDTO> getTopSellingCategories(@AuthenticationPrincipal User authenticatedUser) {
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Seller seller = user.getSeller();
        if (seller == null) {
            throw new RuntimeException("No seller associated with this user");
        }
        return categoryService.getTopSellingCategoriesBySeller(seller.getId().longValue());
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getAllProducts(id));
    }

    @GetMapping("/products")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesWithProducts(){
        List<CategoryDTO> catDto =  categoryService.getAllCategories();
        for(CategoryDTO catDto1 : catDto){
            catDto1.setProducts(productService.getAllProducts(catDto1.getId()));
        }
       return ResponseEntity.ok(catDto);
    }

}