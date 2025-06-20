package com.Ecommerce.backend;

public class VariantDto {
    private int variantId;
    private String color;
    private String siz;
    private double variantWeight;
    private double variantPrice;

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSiz() {
        return siz;
    }

    public void setSiz(String siz) {
        this.siz = siz;
    }

    public double getVariantWeight() {
        return variantWeight;
    }

    public void setVariantWeight(double variantWeight) {
        this.variantWeight = variantWeight;
    }

    public double getVariantPrice() {
        return variantPrice;
    }

    public void setVariantPrice(double variantPrice) {
        this.variantPrice = variantPrice;
    }
}
