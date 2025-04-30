package com.test.security.product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.test.security.category.Category;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    private double price;
    private String description;
    private String image;
    @Column(nullable = true)
    private  Long quantityAvailable;
    @Column(nullable = true)
    private Boolean specialOffer;
    @Column(nullable = true)
    private String hardwareSpecifications;


    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

//    @Column(name = "seller_id" , nullable = false);
//    private long sellerId;





    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
    public Long getQuantityAvailable() {
        return quantityAvailable;
    }
    public void setQuantityAvailable(Long quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public Boolean getSpecialOffer() {
        return specialOffer;
    }

    public void setSpecialOffer(Boolean specialOffer) {
        this.specialOffer = specialOffer;
    }

    public String getHardwareSpecifications() {
        return hardwareSpecifications;
    }
    public void setHardwareSpecifications(String hardwareSpecifications) {
        this.hardwareSpecifications = hardwareSpecifications;
    }



}
