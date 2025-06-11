package com.test.security.transaction;

import com.test.security.user.Role;
import com.test.security.user.User;
import com.test.security.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction transaction) {
        return transactionService.createTransaction(transaction);
    }

    //get transaction for seller
    @GetMapping("/seller/{sellerId}")
    public List<Transaction> getTransactionsForSeller(@PathVariable Long sellerId,
                                                      @AuthenticationPrincipal User authenticatedUser) {
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if((user.getRole().equals(Role.ROLE_SELLER) && user.getSeller().getId().equals(sellerId)) || user.getRole().equals(Role.ROLE_ADMIN)) {
            return transactionService.getTransactionsForSeller(sellerId);
        }
        return null;
    }
    
    
    
}
