package com.Ecommerce.backend;

import jakarta.persistence.*;

@Entity
public class ProductVariant {
    @Id
            @Column(name = "productvariant_id")
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "productvariant_seq_gen"
    )
    @SequenceGenerator(
            name = "productvariant_seq_gen",
            sequenceName = "seq_productvariant",
            allocationSize = 1
    )
    Integer productVariantId;
    Integer productId;
    Integer variantId;

    public ProductVariant() {
    }

    public ProductVariant(Integer productId, Integer variantId) {
        this.productId = productId;
        this.variantId = variantId;
    }

    public Integer getProductVariantId() {
        return productVariantId;
    }

    public void setProductVariantId(Integer productVariantId) {
        this.productVariantId = productVariantId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getVariantId() {
        return variantId;
    }

    public void setVariantId(Integer variantId) {
        this.variantId = variantId;
    }
}
