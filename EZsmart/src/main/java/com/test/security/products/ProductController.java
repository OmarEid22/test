package com.test.security.products;

import com.test.security.category.Category;
import com.test.security.category.CategoryDTO;
import com.test.security.category.CategoryMapper;
import com.test.security.category.CategoryService;
import com.test.security.product.Product;

import com.test.security.seller.Seller;
import com.test.security.seller.SellerRepository;
import com.test.security.seller.SellerService;
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
    private final SellerService sellerService;
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    public ProductController(ProductService productService, UserRepository userRepository, SellerService sellerService, CategoryService categoryService , CategoryMapper categoryMapper) {
        this.productService = productService;
        this.userRepository = userRepository;
        this.sellerService = sellerService;
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
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
        Long categoryId = product.getCategory().getId();
        CategoryDTO categoryDTO = categoryService.getCategoryById(categoryId);
        if (categoryDTO == null) {
            throw new RuntimeException("Category not found");
        }
        Category category = categoryMapper.toEntity(categoryDTO);
        product.setCategory(category);
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
        if(productService.getProductById(productId).isEmpty())
            throw new RuntimeException("Product not found");
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

    //user gets specific seller products
    @GetMapping("/sellerProducts/{sellerId}")
    public List<Product> getSellerProducts(@PathVariable int sellerId) {
       
        Seller seller = sellerService.getSellerById(sellerId);
        if(seller == null)throw new RuntimeException("No seller with the specified ID");
        return productService.getProductsBySeller(seller.getId().longValue());
    }

    //get product by category, dicountPrice, specialOffer and price range
    @GetMapping("/filter")
    public List<Product> filterProducts(@RequestParam(required = false) Long categoryId,
                                        @RequestParam(required = false) Double discountPrice,
                                        @RequestParam(required = false) Boolean specialOffer,
                                        @RequestParam(required = false) Double priceRangeMin,
                                        @RequestParam(required = false) Double priceRangeMax,
                                        @RequestParam(required = false) String sortBy) {
                                            System.out.println(categoryId);
        System.out.println(discountPrice);
        System.out.println(specialOffer);
        System.out.println(priceRangeMin);
        System.out.println(priceRangeMax);
        System.out.println(sortBy);
        return productService.searchProducts(categoryId, discountPrice, specialOffer, priceRangeMin, priceRangeMax , sortBy);
    
    }

    //search products by name
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String name) {
        return productService.searchProductsByName(name);
    }
        
}
