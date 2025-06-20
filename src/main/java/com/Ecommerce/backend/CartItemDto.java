package com.Ecommerce.backend;

public class CartItemDto {
    private int cartVariantId;
    private int cartId;
    private Integer productId;    // newly added
    private Integer variantId;    // now nullable
    private String productName;
    private String color;
    private String siz;
    private double variantPrice;  // holds either variant_price or base_price
    private int quantity;

    // --- Getters & setters ---

    public int getCartVariantId() {
        return cartVariantId;
    }
    public void setCartVariantId(int cartVariantId) {
        this.cartVariantId = cartVariantId;
    }

    public int getCartId() {
        return cartId;
    }
    public void setCartId(int cartId) {
        this.cartId = cartId;
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

    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
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

    public double getVariantPrice() {
        return variantPrice;
    }
    public void setVariantPrice(double variantPrice) {
        this.variantPrice = variantPrice;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
