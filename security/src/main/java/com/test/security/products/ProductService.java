package com.test.security.products;
import com.test.security.product.Product;
import com.test.security.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts(Long categoryId, Double minPrice, Double maxPrice) {
        return productRepository.findProducts(categoryId, minPrice, maxPrice);
    }

    public Optional<Product> getProductById(Long productId) {
        return productRepository.findById(productId);
    }

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> updateProduct(Long productId, Product updatedProduct) {
        return productRepository.findById(productId).map(existingProduct -> {
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setDescription(updatedProduct.getDescription());
            existingProduct.setImage(updatedProduct.getImage());
            return productRepository.save(existingProduct);
        });
    }

    public void deleteProduct(Long productId) {
        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);
        }
    }
}
