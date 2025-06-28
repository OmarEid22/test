package com.test.security.feedback;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import com.test.security.product.Product;
import com.test.security.user.User;
import com.test.security.user.UserRepository;
import com.test.security.product.ProductRepository;

@RequiredArgsConstructor
@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public Feedback getFeedbackById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
    }

    public List<Feedback> getFeedbacksByUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return feedbackRepository.findByUserId(user.getId());
    }

    public List<Feedback> getFeedbacksByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return feedbackRepository.findByProductId(productId);
    }

    public Feedback addFeedback(User user, FeedbackRequest request , Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Feedback feedback = Feedback.builder()
                .user(user)
                .product(product)
                .comment(request.getComment())
                .rating(request.getRating())
                .image(request.getImage())
                .build();
        return feedbackRepository.save(feedback);
    }

    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }


}
