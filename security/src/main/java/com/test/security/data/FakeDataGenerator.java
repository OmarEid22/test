package com.test.security.data;

            import com.github.javafaker.Faker;
            import com.test.security.category.Category;
            import com.test.security.category.CategoryRepository;
            import com.test.security.product.Product;
            import com.test.security.product.ProductRepository;
            import org.springframework.beans.factory.annotation.Autowired;
            import org.springframework.stereotype.Component;

            import javax.annotation.PostConstruct;
            import java.util.HashSet;
            import java.util.List;
            import java.util.Random;
            import java.util.Set;
            import java.util.stream.Collectors;
            import java.util.stream.IntStream;

            @Component
            public class FakeDataGenerator {

                @Autowired
                private ProductRepository productRepository;

                @Autowired
                private CategoryRepository categoryRepository;

                @PostConstruct
                public void generateFakeData() {
                    Faker faker = new Faker();
                    Random random = new Random();

                    // Generate and save categories first
                    List<Category> categories = IntStream.range(0, 5)
                            .mapToObj(i -> Category.builder()
                                    .name(faker.commerce().department())
                                    .build())
                            .collect(Collectors.toList());

                    categoryRepository.saveAll(categories);

                    // Generate products with random categories
                    List<Product> products = IntStream.range(0, 20)
                            .mapToObj(i -> {
                                // Randomly select a category for the product
                                return Product.builder()
                                        .name(faker.commerce().productName())
                                        .description(faker.lorem().sentence())
                                        .price(20 + random.nextDouble() * 980) // Price between 20 and 1000
                                        .image("product" + (i + 1) + ".jpg")
                                        .quantityAvailable(Long.valueOf(random.nextInt(100) + 1))
                                        .specialOffer(random.nextBoolean())
                                        .hardwareSpecifications(faker.lorem().sentence())
                                        .category(categories.get(random.nextInt(categories.size())))
                                        .build();
                            })
                            .collect(Collectors.toList());

                    productRepository.saveAll(products);
                }
            }