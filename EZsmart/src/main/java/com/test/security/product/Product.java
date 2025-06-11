package com.test.security.product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.test.security.seller.Seller;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.test.security.category.Category;
import com.test.security.user.User;
import com.test.security.orderItem.OrderItem;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private Double price;
    @Column(nullable = true)
    private Double sellingPrice;
    private String description;
    private String image;
    @Column(nullable = true)
    private  Long quantityAvailable;
    @Column(nullable = true)
    private Boolean specialOffer;
    @Column(nullable = true)
    private String hardwareSpecifications;
    @Column(nullable = true)
    private Double discountPrice;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "seller_id",referencedColumnName = "id", nullable = false)
    private Seller seller;

    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;


}
