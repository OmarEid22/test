package com.test.security.seller;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SellerService {
    private final SellerRepository sellerRepository;

    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    public Seller createSeller(Seller seller) {
        // Set default status when creating a seller
        seller.setStatus(SellerStatus.PENDING_VERIFICATION);
        return sellerRepository.save(seller);
    }

    public Seller getSellerById(Integer id) {
        return sellerRepository.findById(id).orElse(null);
    }

    public void deleteSeller(Integer id) {
        sellerRepository.deleteById(id);
    }

    //get all sellers
    public List<Seller> getAllSellers() {
        return sellerRepository.findAll();
    }
    
    // Get sellers by status
    public List<Seller> getSellersByStatus(SellerStatus status) {
        return sellerRepository.findByStatus(status);
    }
    
    @Transactional
    public Optional<Seller> updateSellerStatus(Integer id, SellerStatus status) {
        Optional<Seller> optionalSeller = sellerRepository.findById(id);
        if (optionalSeller.isPresent()) {
            Seller seller = optionalSeller.get();
            seller.setStatus(status);
            Seller updatedSeller = sellerRepository.save(seller);
            return Optional.of(updatedSeller);
        }
        return Optional.empty();
    }
}
