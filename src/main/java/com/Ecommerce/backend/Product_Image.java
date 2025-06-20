package com.Ecommerce.backend;

import jakarta.persistence.*;
import org.springframework.web.multipart.MultipartFile;

@Entity
public class Product_Image {
    @Id
    @Column(name = "product_image_id")
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "product_seq_gen"
    )
    @SequenceGenerator(
            name = "product_seq_gen",
            sequenceName = "seq_product",
            allocationSize = 1
    )

    Integer productImageId;
    @Column(name = "product_id")
    Integer productId;
    @Column(name = "image_url")
    String imageUrl;

    public Product_Image() {
    }

    public Product_Image(Integer productId, String imageUrl) {
        this.productId = productId;
        this.imageUrl = imageUrl;
    }

    public Integer getProductImageId() {
        return productImageId;
    }

    public void setProductImageId(Integer productImageId) {
        this.productImageId = productImageId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
