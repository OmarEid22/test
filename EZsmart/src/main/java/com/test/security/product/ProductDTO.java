//package com.test.security.product;
//
//import com.test.security.category.CategoryDTO;
//
//import java.util.HashSet;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//public class ProductDTO {
//    private Long id;
//    private String name;
//    private double price;
//    private String description;
//    private String image;
//    private Long quantityAvailable;
//    private Boolean specialOffer;
//    private String hardwareSpecifications;
//    private Set<CategoryDTO> categories = new HashSet<>();
//
//    public ProductDTO() {}
//
//    public ProductDTO(Product product) {
//        this.id = product.getId();
//        this.name = product.getName();
//        this.price = product.getPrice();
//        this.description = product.getDescription();
//        this.image = product.getImage();
//        this.quantityAvailable = product.getQuantityAvailable();
//        this.specialOffer = product.getSpecialOffer();
//        this.hardwareSpecifications = product.getHardwareSpecifications();
//
//        // Only include basic category information to avoid recursion
//        if (product.getCategories() != null) {
//            this.categories = product.getCategories().stream()
//                    .map(category -> new CategoryDTO(category))
//                    .collect(Collectors.toSet());
//        }
//    }
//
//    // Getters and setters
//    // [Include all getters and setters here]
//}