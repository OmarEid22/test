package com.test.security.feedback;

import com.test.security.user.Role;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.test.security.user.User;

@AllArgsConstructor
@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping("/user/{userId}")
    public List<Feedback> getUserFeedbacks(@AuthenticationPrincipal User user , @PathVariable Integer userId) {
        return feedbackService.getFeedbacksByUser(userId);
    }

    @GetMapping("/product/{productId}")
    public List<Feedback> getFeedbacksByProduct(@PathVariable Long productId) {
        return feedbackService.getFeedbacksByProduct(productId);
    }

    @PostMapping("/{productId}")
    public Feedback addFeedback(@AuthenticationPrincipal User user, @RequestBody FeedbackRequest request , @PathVariable Long productId) {
        return feedbackService.addFeedback(user, request , productId);
    }

    @DeleteMapping("/{id}")
    public void deleteFeedback(@AuthenticationPrincipal User user, @PathVariable Long id) {
        if(!feedbackService.getFeedbackById(id).getUser().equals(user) && !user.getRole().equals(Role.ROLE_ADMIN)) {
            throw new RuntimeException("You are not authorized to delete this feedback");
        }
        feedbackService.deleteFeedback(id);
    }
}
