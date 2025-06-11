package com.test.security.profile;

import com.test.security.seller.Seller;
import com.test.security.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
    private Long id;
    private Integer userId;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Seller information
    private SellerDTO seller;

    //user information
    private String firstname;
    private String lastname;
    private String email;
    private String mobile;
    private List<Map<String, Object>> addresses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerDTO {

        private int id;
        private String sellerName;
        private String businessName;
        private String mobile;
        private String mail;
        private String bankAccountNumber;
        private String bankAccountHolderName;
        private String TIN;
        private String swiftCode;
        private String logo;
        private String banner;
        private Map<String, Object> address;

        public static SellerDTO fromSeller(Seller seller) {
            if (seller == null) return null;
            
            return SellerDTO.builder()
                    .id(seller.getId())
                    .sellerName(seller.getName())
                    .businessName(seller.getBusinessName())
                    .mobile(seller.getMobile())
                    .mail(seller.getMail())
                    .bankAccountNumber(seller.getBankAccountNumber())
                    .bankAccountHolderName(seller.getBankAccountHolderName())
                    .TIN(seller.getTIN())
                    .swiftCode(seller.getSwiftCode())
                    .logo(seller.getLogo())
                    .banner(seller.getBanner())
                    .address(seller.getAddress())
                    .build();
        }
    }



    public static ProfileDTO fromProfile(Profile profile) {
        ProfileDTO dto = new ProfileDTO();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUser().getId());
        dto.setImage(profile.getImage());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        
        // Include seller information if available
        User user = profile.getUser();
        if (user != null && user.getSeller() != null) {
            dto.setSeller(SellerDTO.fromSeller(user.getSeller()));
        } else if (user != null && user.getSeller() == null) {
            dto.setFirstname(profile.getUser().getFirstname());
            dto.setLastname(profile.getUser().getLastname());
            dto.setEmail(profile.getUser().getEmail());
            dto.setMobile(profile.getUser().getMobile());
            dto.setAddresses(profile.getUser().getAddresses());
        }

        return dto;
    }
} 