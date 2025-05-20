package com.test.security.seller;

import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class SellerService {
    private final SellerRepository sellerRepository;

    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    public Seller createSeller(Seller seller) {
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



}
