package com.test.security.seller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.test.security.product.Product;
import com.test.security.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.json.JsonType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sellers")
public class Seller {

    @Id
    @GeneratedValue
    private int id;

    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @ToString.Exclude
    private User user;

    //name , mobie , mail , bankAccountNumber , bankAccountHolderName , TIN , swiftCode , logo , banner
    private String name;
    private String businessName;
    private String mobile;
    private String mail;
    private String bankAccountNumber;
    private String bankAccountHolderName;
    private String TIN;
    private String swiftCode;
    private String logo;
    private String banner;

    //list of addresses
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> address;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Product> products = new ArrayList<>();

    //getters and setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

//    public User getUser() {
//        return user;
//    }
//    public void setUser(User user) {
//        this.user = user;
//    }


}
