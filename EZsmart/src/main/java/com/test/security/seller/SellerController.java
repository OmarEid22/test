package com.test.security.seller;


import com.test.security.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final SellerService sellerService;
    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    public SellerController(SellerService sellerService, UserRepository userRepository, UserService userService) {
        this.sellerService = sellerService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Seller getSellerById(@PathVariable Integer id) {
        return sellerService.getSellerById(id);
    }

    @GetMapping
    @ResponseBody
    public List<Seller> getAllSellers(@RequestParam(required = false) SellerStatus status, 
                                      @AuthenticationPrincipal User authenticatedUser) {
        // If status filter is provided, verify admin role
        if (status != null) {
            if (!authenticatedUser.getRole().equals(Role.ROLE_ADMIN)) {
                throw new RuntimeException("Only administrators can filter sellers by status");
            }
            return sellerService.getSellersByStatus(status);
        }
        
        // No status filter, return all sellers
        return sellerService.getAllSellers();
    }

    // Create a new seller
    @PostMapping
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public Seller createSeller(@RequestBody Seller seller, @AuthenticationPrincipal User authenticatedUser) {
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole().equals(Role.ROLE_SELLER)) {
            throw new RuntimeException("User is already a seller");
        } else if (user.getRole().equals(Role.ROLE_ADMIN)) {
            throw new RuntimeException("User is an admin");
        } else {
            seller.setUser(user);
            Seller newSeller = sellerService.createSeller(seller);
            userService.updateUserRoleAndSeller(user, Role.ROLE_SELLER, seller);
            return newSeller;
        }
    }

    @PutMapping("/{id}/status")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Seller> updateSellerStatus(
            @PathVariable Integer id,
            @RequestBody SellerStatusRequest statusRequest) {
        return sellerService.updateSellerStatus(id, statusRequest.getStatus())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //delete seller
    @DeleteMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SELLER')")
    public void deleteSeller(@AuthenticationPrincipal User authenticatedUser, @PathVariable Integer id) {
        if (!authenticatedUser.getRole().equals(Role.ROLE_SELLER) && !authenticatedUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new RuntimeException("You are not authorized to delete this seller.");
        }
        Integer sellerId = authenticatedUser.getRole().equals(Role.ROLE_SELLER) ? authenticatedUser.getSeller().getId() : id;
        System.out.println("Authenticated user: " + authenticatedUser.getEmail());
        System.out.println("Role: " + authenticatedUser.getRole());
        System.out.println("Path ID: " + id);
        System.out.println("Deleting seller with ID: " + sellerId);
        sellerService.deleteSeller(sellerId);
    }

    //get seller by seller status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseBody
    public List<Seller> getSellersByStatus(@PathVariable SellerStatus status) {
        return sellerService.getSellersByStatus(status);
    }

    //get seller status names
    @GetMapping("/status")
    @ResponseBody
    public List<String> getSellerStatusNames() {
        return Arrays.stream(SellerStatus.values())
                .map(SellerStatus::name)
                .collect(Collectors.toList());
    }

}
