//package com.test.security.data;
//
//import com.github.javafaker.Faker;
//import com.test.security.category.Category;
//import com.test.security.category.CategoryRepository;
//import com.test.security.product.Product;
//import com.test.security.product.ProductRepository;
//import com.test.security.seller.Seller;
//import com.test.security.user.Role;
//import com.test.security.user.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//@Component
//public class FakeDataGenerator {
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    @PostConstruct
//    public void generateFakeData() {
//        Faker faker = new Faker();
//        Random random = new Random();
//
//        // Generate and save categories first
//        List<Category> categories = IntStream.range(0, 5)
//                .mapToObj(i -> Category.builder()
//                        .name(faker.commerce().department())
//                        .build())
//                .collect(Collectors.toList());
//
//        categoryRepository.saveAll(categories);
//
//        // Generate products with random categories
//        List<Product> products = IntStream.range(0, 20)
//                .mapToObj(i -> {
//                    // Randomly select a category for the product
//                    return Product.builder()
//                            .name(faker.commerce().productName())
//                            .description(faker.lorem().sentence())
//                            .price(20 + random.nextDouble() * 980) // Price between 20 and 1000
//                            .image("product" + (i + 1) + ".jpg")
//                            .quantityAvailable((long) (random.nextInt(100) + 1))
//                            .specialOffer(random.nextBoolean())
//                            .hardwareSpecifications(faker.lorem().sentence())
//                            .category(categories.get(random.nextInt(categories.size())))
//                            .seller(new Seller(
//                                    random.nextInt(5) + 1,
//                                    new User(
//                                            1,
//                                            faker.name().firstName(),
//                                            faker.name().lastName(),
//                                            faker.internet().emailAddress(),
//                                            faker.internet().password(),
//                                            faker.phoneNumber().phoneNumber(),
//                                            Role.ROLE_SELLER,
//                                            null,
//                                            new ArrayList<>()
//                                    ),
//                                    faker.name().fullName(),
//                                    faker.phoneNumber().phoneNumber(),
//                                    faker.internet().emailAddress(),
//                                    faker.address().fullAddress(),
//                                    faker.company().name(),
//                                    faker.company().bs(),
//                                    new ArrayList<>()
//                            ))
//                            .build();
//                })
//                .collect(Collectors.toList());
//
//        productRepository.saveAll(products);
//    }
//}