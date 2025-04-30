package com.test.security.products;
import com.test.security.product.Product;
import com.test.security.product.ProductRepository;
import org.hibernate.Hibernate;
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
            existingProduct.setQuantityAvailable(updatedProduct.getQuantityAvailable());
            existingProduct.setSpecialOffer(updatedProduct.getSpecialOffer());
            existingProduct.setCategory(updatedProduct.getCategory());
            existingProduct.setHardwareSpecifications(updatedProduct.getHardwareSpecifications());
            existingProduct.setSpecialOffer(updatedProduct.getSpecialOffer());
            return productRepository.save(existingProduct);
        });
    }

    public void deleteProduct(Long productId) {
        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);
        }
    }
}
