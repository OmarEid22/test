package com.test.security.products;

import com.test.security.product.Product;
import com.test.security.product.ProductRepository;
import com.test.security.seller.Seller;
import org.hibernate.Hibernate;
import com.test.security.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts(Long categoryId) {
        List<Product> products = productRepository.findProducts(categoryId);

        // Initialize the category to prevent lazy loading during serialization
        for (Product product : products) {
            Hibernate.initialize(product.getCategory());
        }

        return products;
    }


    // Specific product to all
    public Optional<Product> getProductById(Long productId) {
        return productRepository.findById(productId);
    }

    // Seller adds a product for themselves
    public Product addProductAsSeller(Product product) {
        return productRepository.save(product);
    }

    // Admin adds a product with or without assigning a seller
    public Product addProductAsAdmin(Product product, Seller seller) {
        return productRepository.save(product);
    }

    // Seller updating only their product
    public Optional<Product> updateProduct(Long productId, Product updatedProduct, Long sellerId) {
        Product existingProduct = productRepository.findByIdAndSellerId(productId, sellerId);
        if (existingProduct == null) {
            return Optional.empty();
        }
        updateProductFields(existingProduct, updatedProduct);
        return Optional.of(productRepository.save(existingProduct));
    }

    // Admin updating any product
    public Optional<Product> updateProductAsAdmin(Long productId, Product updatedProduct) {
        return productRepository.findById(productId).map(existingProduct -> {
            updateProductFields(existingProduct, updatedProduct);
            return productRepository.save(existingProduct);
        });
    }

    // Seller deletes only their product
    public boolean deleteProduct(Long productId, Long sellerId) {
        Product product = productRepository.findByIdAndSellerId(productId, sellerId);
        if (product != null) {
            productRepository.deleteById(productId);
            return true;
        }
        return false;
    }

    // Admin deletes any product
    public void deleteProductAsAdmin(Long productId) {
        productRepository.deleteById(productId);
    }

    // Seller views their products
    public List<Product> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    //get product by category, dicountPrice, specialOffer and price range
    //all fields are optional
    public List<Product> searchProducts(Long categoryId, Double discountPrice, Boolean specialOffer, Double priceRangeMin, Double priceRangeMax) {
        return productRepository.searchProducts(categoryId, discountPrice, specialOffer, priceRangeMin, priceRangeMax);
    }

    // Shared update logic
    private void updateProductFields(Product existingProduct, Product updatedProduct) {
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setSellingPrice(updatedProduct.getSellingPrice());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setImage(updatedProduct.getImage());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setQuantityAvailable(updatedProduct.getQuantityAvailable());
        existingProduct.setSpecialOffer(updatedProduct.getSpecialOffer());
        existingProduct.setDiscountPrice(updatedProduct.getDiscountPrice());
        existingProduct.setHardwareSpecifications(updatedProduct.getHardwareSpecifications());
    }
}
