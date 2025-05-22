package com.test.security.products;

import com.test.security.product.Product;

import com.test.security.seller.Seller;
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


    @GetMapping
    public List<Product> getAllProducts(@RequestParam(required = false) Long categoryId) {
        return productService.getAllProducts(categoryId);
    }

    @GetMapping("/{productId}")
    public Optional<Product> getProductById(@PathVariable Long productId) {
        return productService.getProductById(productId);
    }

    // seller to his
    @PostMapping("/seller")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public Product addProductAsSeller(@RequestBody Product product,
                                      @AuthenticationPrincipal User authenticatedUser) {
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Seller seller = user.getSeller();
        if (seller == null) {
            throw new RuntimeException("No seller associated with this user");
        }
        product.setSeller(seller);
        return productService.addProductAsSeller(product);
    }

    // admin to seller or no seller
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Product addProductAsAdmin(@RequestBody Product product) {
        return productService.addProductAsAdmin(product, null);
    }

    // seller updating his
    @PutMapping("/seller/{productId}")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public Optional<Product> updateProductAsSeller(@PathVariable Long productId,
                                                   @RequestBody Product product,
                                                   @AuthenticationPrincipal User authenticatedUser) {
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Seller seller = user.getSeller();
        if (seller == null) {
            throw new RuntimeException("No seller associated with this user");
        }
        return productService.updateProduct(productId, product, seller.getId().longValue());
    }

    // admin updating any
    @PutMapping("/admin/{productId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Optional<Product> updateProductAsAdmin(@PathVariable Long productId,
                                                  @RequestBody Product product) {
        return productService.updateProductAsAdmin(productId, product);
    }

    //seller deleting only his
    @DeleteMapping("/seller/{productId}")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public void deleteProductAsSeller(@PathVariable Long productId,
                                      @AuthenticationPrincipal User authenticatedUser) {
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Seller seller = user.getSeller();
        if (seller == null) {
            throw new RuntimeException("No seller associated with this user");
        }
        productService.deleteProduct(productId, seller.getId().longValue());
    }

    //admin deleting any
    @DeleteMapping("/admin/{productId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteProductAsAdmin(@PathVariable Long productId) {
        productService.deleteProductAsAdmin(productId);
    }

    //seller see only his
    @GetMapping("/seller")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public List<Product> getSellerProducts(@AuthenticationPrincipal User authenticatedUser) {
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Seller seller = user.getSeller();
        if (seller == null) {
            throw new RuntimeException("No seller associated with this user");
        }
        return productService.getProductsBySeller(seller.getId().longValue());
    }
}
