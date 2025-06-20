package com.Ecommerce.backend;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Combined REST and page‐forwarding controller.
 */
@Controller
public class Econtroller {
    private final CustomerService customerService;

    @Autowired
    public Econtroller(CustomerService customerService) {
        this.customerService = customerService;
    }

    /** Process signup POST */
    @PostMapping("/signup")
    public ResponseEntity<String> doSignup(@ModelAttribute Customer customer) {
        int newCustomerId = customerService.register(customer);
        customer.setCustomerId(newCustomerId);
        return ResponseEntity.ok("Customer registered");
    }

    @PostMapping(path = "/login")
    public ResponseEntity<String> LogIn(
            @RequestParam String userName,
            @RequestParam String password,
            HttpSession session
    ) {
        Integer verifier = customerService.authenticate(userName, password);
        if (verifier == null || verifier == 0) {
            // invalid credentials → redirect to signup
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/SignUp.html"))
                    .build();
        }

        // successful login → fetch the actual customer_id
        Integer customerId = customerService.findCustomerIdByUserName(userName);


        // store in session for all downstream handlers
        session.setAttribute("customerId", customerId);

        // redirect to home
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/Home.html"))
                .build();
    }


    // --- Existing REST endpoints ---

    @PostMapping(path = "/addProducts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<String> addProducts(
            @Valid @ModelAttribute DtoAddProduct dto,
            BindingResult br) throws IOException {

        if (br.hasErrors()) {
            String errors = br.getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body("Validation failed: " + errors);
        }

        int categoryId = customerService.getOrCreateCategoryId(dto.getCategoryName());
        int productId  = customerService.insertProduct(dto, categoryId);
        customerService.insertProductImage(dto, productId);

        return ResponseEntity.ok("Product added successfully");
    }

    @GetMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ProductDto> listProducts() {
        return customerService.listAllProducts();
    }

    @GetMapping(path = "/product/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ProductDetailDto getProductDetail(@PathVariable("id") int id) {
        return customerService.getProductDetail(id);
    }

    /** Add to cart */
    @PostMapping(path = "/cart", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String,Object>> addToCart(
            @RequestBody CartRequest payload,
            @SessionAttribute("customerId") Integer customerId
    ) {
        Integer cartId    = payload.getCartId();
        Integer prodId    = payload.getProductId();
        Integer varId     = payload.getVariantId();
        int     qty       = (payload.getQuantity() == null ? 1 : payload.getQuantity());
        Map<String,Object> resp = new HashMap<>();

        // validation
        if (prodId == null && varId == null) {
            resp.put("error", "Must supply productId or variantId");
            return ResponseEntity.badRequest().body(resp);
        }

        // delegate to service: either reuse existing cart or create one
        int resultingCart = customerService.addOrGetCartAndInsertItem(
                customerId, prodId, varId, qty
        );

        resp.put("cartId", resultingCart);
        resp.put("message", "Item added to cart");
        return ResponseEntity.ok(resp);
    }

    /** Get cart contents */
    @GetMapping(path = "/cart/{cartId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<CartItemDto> getCartContents(@PathVariable("cartId") int cartId) {
        return customerService.getCartContents(cartId);
    }

    /** Update cart item quantity */
    @PutMapping(path = "/cart/{cartId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @PathVariable int cartId,
            @RequestBody Map<String, Integer> payload) {

        // use primitives to match service signature
        int cartVariantId = payload.getOrDefault("cartVariantId", -1);
        int quantity      = payload.getOrDefault("quantity", 0);

        if (cartVariantId < 0 || quantity < 1) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid payload"));
        }
        try {
            customerService.updateCartQuantity(cartVariantId, quantity);
            return ResponseEntity.ok(Map.of("cartId", cartId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** Remove a cart item */
    @DeleteMapping(path = "/cart/{cartId}/{cartVariantId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeCartItem(
            @PathVariable int cartId,
            @PathVariable int cartVariantId) {

        try {
            customerService.removeFromCart(cartVariantId);
            return ResponseEntity.ok(Map.of("cartId", cartId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // --- Forward to checkout page ---
    @GetMapping("/checkout")
    public String forwardToCheckout() {
        return "forward:/checkOut.html";
    }

    @PostMapping(path = "/order", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> placeOrder(
            @RequestBody OrderRequest req,
            @SessionAttribute("customerId") Integer customerId
    ) {
        Map<String, Object> resp = new HashMap<>();
        try {
            // inject the logged-in user’s ID
            req.setCustomerId(customerId);

            int orderId = customerService.placeOrder(req);
            resp.put("orderId", orderId);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
}
