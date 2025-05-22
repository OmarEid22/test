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
    private User user;

    //name , mobie , mail , bankAccountNumber , bankAccountHolderName , TIN , swiftCode , logo , banner
    private String name;
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
    private List<Map<String, Object>> addresses;

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

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMail() {
        return mail;
    }
    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }
    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankAccountHolderName() {
        return bankAccountHolderName;
    }
    public void setBankAccountHolderName(String bankAccountHolderName) {
        this.bankAccountHolderName = bankAccountHolderName;
    }

    public String getTIN() {
        return TIN;
    }
    public void setTIN(String TIN) {
        this.TIN = TIN;
    }

    public String getSwiftCode() {
        return swiftCode;
    }
    public void setSwiftCode(String swiftCode) {
        this.swiftCode = swiftCode;
    }

    public String getLogo() {
        return logo;
    }
    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getBanner() {
        return banner;
    }
    public void setBanner(String banner) {
        this.banner = banner;
    }

//    public List<JsonNode> getAddresses() {
//        return addresses;
//    }
//    public void setAddresses(List<JsonNode> addresses) {
//        this.addresses = addresses;
//    }





}
