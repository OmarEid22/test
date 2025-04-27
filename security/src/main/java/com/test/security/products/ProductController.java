package com.test.security.products;

import com.test.security.product.Product;
import com.test.security.user.User;
import com.test.security.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    // all to all
    @GetMapping
    public List<Product> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return productService.getAllProducts(categoryId, minPrice, maxPrice);
    }

    @GetMapping("/{productId}")
    public Optional<Product> getProductById(@PathVariable Long productId) {
        return productService.getProductById(productId);
    }

    // seller to his
    @PostMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    public Product addProductAsSeller(@RequestBody Product product,
                                      @AuthenticationPrincipal User seller) {
        return productService.addProduct(product, seller);
    }

    // admin to seller or no seller
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Product addProductAsAdmin(@RequestBody Product product) {
        return productService.addProductAsAdmin(product, product.getSeller());
    }

    // seller updating his
    @PutMapping("/seller/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public Optional<Product> updateProductAsSeller(@PathVariable Long productId,
                                                   @RequestBody Product product,
                                                   @AuthenticationPrincipal User seller) {
        return productService.updateProduct(productId, product, seller.getId().longValue());
    }

    // admin updating any
    @PutMapping("/admin/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<Product> updateProductAsAdmin(@PathVariable Long productId,
                                                  @RequestBody Product product) {
        return productService.updateProductAsAdmin(productId, product);
    }

    //seller deleting only his
    @DeleteMapping("/seller/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public void deleteProductAsSeller(@PathVariable Long productId,
                                      @AuthenticationPrincipal User seller) {
        productService.deleteProduct(productId, seller.getId().longValue());
    }

    //admin deleting any
    @DeleteMapping("/admin/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProductAsAdmin(@PathVariable Long productId) {
        productService.deleteProductAsAdmin(productId);
    }

    //seller see only his
    @GetMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    public List<Product> getMyProducts(@AuthenticationPrincipal User seller) {
        return productService.getProductsBySeller(seller.getId().longValue());
    }
}
