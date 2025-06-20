package com.Ecommerce.backend;

// src/main/java/com/Ecommerce/backend/CartRequest.java


public class CartRequest {
    private Integer cartId;
    private Integer productId;
    private Integer variantId;
    private Integer quantity = 1;

    // Getters & setters
    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
