package com.Ecommerce.backend;

import jakarta.persistence.*;
@Entity
public class Variant {
    @Id
    @Column(name = "variant_id")
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "variant_seq_gen"
    )
    @SequenceGenerator(
            name = "variant_seq_gen",
            sequenceName = "seq_variant",
            allocationSize = 1
    )
    Integer variantId;
    String color;
    Double weight;
    String siz;
    @Column(name = "variant_price")
    Double variantPrice;

    public Variant() {
    }



    public Variant(String color, Double weight, String siz, Double variantPrice) {
        this.color = color;
        this.weight = weight;
        this.siz = siz;
        this.variantPrice = variantPrice;
    }



    public String getSiz() {
        return siz;
    }

    public void setSiz(String siz) {
        this.siz = siz;
    }

    public Double getVariantPrice() {
        return variantPrice;
    }

    public void setVariantPrice(Double   variantPrice) {
        this.variantPrice = variantPrice;
    }

    public Integer getVariantId() {
        return variantId;
    }

    public void setVariantId(Integer variantId) {
        this.variantId = variantId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
