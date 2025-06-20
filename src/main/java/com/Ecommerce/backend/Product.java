package com.Ecommerce.backend;

import jakarta.persistence.*;
import org.springframework.web.multipart.MultipartFile;

@Entity
public class Product {
    @Id
            @Column(name = "product_id")
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "product_seq_gen"
    )
    @SequenceGenerator(
            name = "product_seq_gen",
            sequenceName = "seq_product",
            allocationSize = 1
    )
    Integer productId;
    @Column(name = "category_id")
    Integer categoryId;
    @Column(name = "main_image")
    String mainImage;
    String name;
    Double basePrice;
    Double cost;
    String description;
    String manufacturer;
    Double weight;
    public Product() {
    }



    public Product(String mainImage, String name, Double basePrice, Double cost, String description, Double weight, String manufacturer) {
        this.mainImage = mainImage;
        this.name = name;
        this.basePrice = basePrice;
        this.cost = cost;
        this.description = description;
        this.weight=weight;
        this.manufacturer = manufacturer;
    }
    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getMainImage() {
        return mainImage;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
}
