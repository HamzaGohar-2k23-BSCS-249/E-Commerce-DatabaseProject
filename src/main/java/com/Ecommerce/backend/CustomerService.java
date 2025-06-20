    package com.Ecommerce.backend;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.jdbc.core.JdbcTemplate;
    import org.springframework.jdbc.support.GeneratedKeyHolder;
    import org.springframework.jdbc.support.KeyHolder;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.IOException;
    import java.nio.file.*;
    import java.sql.PreparedStatement;
    import java.sql.Types;
    import java.util.List;
    import java.util.UUID;

    @Service
    public class CustomerService {
        private final JdbcTemplate jdbcTemplate;
        private Path uploadDir;

        @Autowired
        public CustomerService(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            try {
                Path appRoot = Paths.get(System.getProperty("user.dir"));
                uploadDir = appRoot.resolve("uploads");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not create upload directory", e);
            }
        }

        public Integer authenticate(String userName, String password) {
            String sql = "SELECT COUNT(*) FROM customer WHERE user_name = ? AND password = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, userName, password);
        }

        public int register(Customer customer) {
            String sql =
                    "INSERT INTO customer " +
                            "(email, password, date_of_birth, gender, first_name, last_name, user_name) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
            KeyHolder kh = new GeneratedKeyHolder();

            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, new String[]{"customer_id"});
                ps.setString(1, customer.getEmail());
                ps.setString(2, customer.getPassword());
                // ← Convert LocalDate → java.sql.Date
                ps.setDate(3, java.sql.Date.valueOf(customer.getDateOfBirth()));
                ps.setString(4, customer.getGender());
                ps.setString(5, customer.getFirstName());
                ps.setString(6, customer.getLastName());
                ps.setString(7, customer.getUserName());
                return ps;
            }, kh);

            Number key = kh.getKey();
            if (key == null) {
                throw new IllegalStateException("No key returned after insert");
            }
            return key.intValue();
        }

        public Integer findCategoryIdByName(String categoryName) {
            String sql = "SELECT category_id FROM category WHERE category_name = ?";
            List<Integer> ids = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> rs.getInt("category_id"),
                    categoryName
            );
            return ids.isEmpty() ? null : ids.get(0);
        }

        public int getOrCreateCategoryId(String categoryName) {
            Integer existing = findCategoryIdByName(categoryName);
            if (existing != null) return existing;

            String insertSql = "INSERT INTO category (category_name) VALUES (?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(insertSql, new String[]{"category_id"});
                ps.setString(1, categoryName);
                return ps;
            }, kh);
            return kh.getKey().intValue();
        }

        public int insertProduct(DtoAddProduct dto, int categoryId) throws IOException {
            MultipartFile mainImage = dto.getMainImage();
            String filename = UUID.randomUUID() + "-" + mainImage.getOriginalFilename();
            Path target = uploadDir.resolve(filename);
            Files.copy(mainImage.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String sql = "INSERT INTO product "
                    + "(name, main_image, description, base_price, cost, weight, manufacturer, category_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, new String[]{"product_id"});
                ps.setString(1, dto.getName());
                ps.setString(2, filename);
                ps.setString(3, dto.getDescription());
                ps.setObject(4, dto.getBasePrice(), Types.DOUBLE);
                ps.setObject(5, dto.getCost(), Types.DOUBLE);
                ps.setObject(6, dto.getWeight(), Types.DOUBLE);
                ps.setString(7, dto.getManufacturer());
                ps.setInt(8, categoryId);
                return ps;
            }, kh);

            int productId = kh.getKey().intValue();
            if (Boolean.TRUE.equals(dto.getHasVariants()) && dto.getVariants() != null) {
                insertVariants(dto.getVariants(), productId);
            }
            return productId;
        }

        public void insertVariants(List<VariantDto> variants, int productId) {
            if (variants == null || variants.isEmpty()) return;

            String insertSql = "INSERT INTO variant (color, variant_weight, siz, variant_price) VALUES (?, ?, ?, ?)";
            String bridgeSql = "INSERT INTO productvariant (product_id, variant_id) VALUES (?, ?)";

            for (VariantDto v : variants) {
                KeyHolder kh = new GeneratedKeyHolder();
                jdbcTemplate.update(conn -> {
                    PreparedStatement ps = conn.prepareStatement(insertSql, new String[]{"variant_id"});
                    ps.setString(1, v.getColor());
                    ps.setObject(2, v.getVariantWeight(), Types.DOUBLE);
                    ps.setString(3, v.getSiz());
                    ps.setObject(4, v.getVariantPrice(), Types.DOUBLE);
                    return ps;
                }, kh);

                int variantId = kh.getKey().intValue();
                jdbcTemplate.update(bridgeSql, productId, variantId);
            }
        }

        public int insertProductImage(DtoAddProduct dto, int productId) throws IOException {
            MultipartFile extraImage = dto.getImageUrl();
            String filename = UUID.randomUUID() + "-" + extraImage.getOriginalFilename();
            Path target = uploadDir.resolve(filename);
            Files.copy(extraImage.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String sql = "INSERT INTO product_image (image_url, product_id) VALUES (?, ?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, new String[]{"product_image_id"});
                ps.setString(1, filename);
                ps.setInt(2, productId);
                return ps;
            }, kh);
            return kh.getKey().intValue();
        }

        public List<ProductDto> listAllProducts() {
            String sql = "SELECT product_id, name, main_image, base_price FROM product";
            return jdbcTemplate.query(sql, (rs, rn) -> {
                ProductDto p = new ProductDto();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setMainImage(rs.getString("main_image"));
                p.setBasePrice(rs.getDouble("base_price"));
                return p;
            });
        }

        public ProductDetailDto getProductDetail(int productId) {
            String prodSql = "SELECT p.product_id, p.name, p.description, p.base_price, p.main_image, "
                    + "c.category_name "
                    + "FROM product p JOIN category c ON p.category_id = c.category_id "
                    + "WHERE p.product_id = ?";
            ProductDetailDto dto = jdbcTemplate.queryForObject(prodSql, (rs, i) -> {
                ProductDetailDto d = new ProductDetailDto();
                d.setProductId(rs.getInt("product_id"));
                d.setName(rs.getString("name"));
                d.setDescription(rs.getString("description"));
                d.setBasePrice(rs.getDouble("base_price"));
                d.setMainImage(rs.getString("main_image"));
                d.setCategoryName(rs.getString("category_name"));
                return d;
            }, productId);

            String varSql = "SELECT v.variant_id, v.color, v.siz, v.variant_weight, v.variant_price "
                    + "FROM variant v JOIN productvariant pv ON v.variant_id = pv.variant_id "
                    + "WHERE pv.product_id = ? ORDER BY v.variant_id";
            List<VariantDto> variants = jdbcTemplate.query(varSql, (rs, i) -> {
                VariantDto v = new VariantDto();
                v.setVariantId(rs.getInt("variant_id"));
                v.setColor(rs.getString("color"));
                v.setSiz(rs.getString("siz"));
                v.setVariantWeight(rs.getDouble("variant_weight"));
                v.setVariantPrice(rs.getDouble("variant_price"));
                return v;
            }, productId);
            dto.setVariants(variants);
            dto.setHasVariants(!variants.isEmpty());

            String imgSql = "SELECT image_url FROM product_image WHERE product_id = ? ORDER BY product_image_id";
            dto.setExtraImages(jdbcTemplate.queryForList(imgSql, String.class, productId));

            return dto;
        }



        /**
         * Returns cart details for checkout.
         */
        /**
         * Returns cart details (both pure‐product and variant items) for the given cartId.
         */
        public List<CartItemDto> getCartContents(int cartId) {
            String sql =
                    "SELECT "
                            + "  cv.cartvariant_id, "
                            + "  cv.cart_id, "
                            + "  cv.quantity, "
                            + "  v.variant_id, "
                            + "  v.color, "
                            + "  v.siz, "
                            + "  -- pick the right price: variant_price if present, else base_price  \n"
                            + "  COALESCE(v.variant_price, p1.base_price) AS price, "
                            + "  -- now coalesce product name from p1 (direct) or p2 (via variant bridge)  \n"
                            + "  COALESCE(p1.name, p2.name) AS product_name "
                            + "FROM cartvariant cv "
                            + "LEFT JOIN variant v         ON cv.variant_id = v.variant_id "
                            + "LEFT JOIN product p1        ON cv.product_id = p1.product_id "
                            + "LEFT JOIN productvariant pv ON pv.variant_id = v.variant_id "
                            + "LEFT JOIN product p2        ON pv.product_id = p2.product_id "
                            + "WHERE cv.cart_id = ?";

            return jdbcTemplate.query(sql, (rs, i) -> {
                CartItemDto dto = new CartItemDto();
                dto.setCartVariantId(rs.getInt("cartvariant_id"));
                dto.setCartId       (rs.getInt("cart_id"));
                dto.setVariantId    (rs.getObject("variant_id") == null
                        ? null
                        : rs.getInt("variant_id"));
                dto.setProductName  (rs.getString("product_name"));
                dto.setColor        (rs.getString("color"));
                dto.setSiz          (rs.getString("siz"));
                dto.setVariantPrice (rs.getDouble("price"));
                dto.setQuantity     (rs.getInt("quantity"));
                return dto;
            }, cartId);
        }



            /**
             * 1) persist shipping_address and return its PK
             */
            public int saveShippingAddress(int customerId, ShippingAddressDto s) {
                String sql = """
            INSERT INTO shippingAddress
              (customer_id, first_name, last_name, phone_number, address, province, city, area)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
                KeyHolder kh = new GeneratedKeyHolder();
                jdbcTemplate.update(conn -> {
                    PreparedStatement ps = conn.prepareStatement(sql, new String[]{"shipping_address_id"});
                    ps.setInt(1, customerId);
                    ps.setString(2, s.getFirstName());
                    ps.setString(3, s.getLastName());
                    ps.setString(4, s.getPhone());
                    ps.setString(5, s.getAddress());
                    ps.setString(6, s.getProvince());
                    ps.setString(7, s.getCity());
                    ps.setString(8, s.getArea());
                    return ps;
                }, kh);
                return kh.getKey().intValue();
            }
        /**
         * 2) persist order2 and return its PK
         */
        public int saveOrder(int customerId, int cartId, int shippingAddressId, double total) {
            String sql = """
            INSERT INTO order2
              (customer_id, cart_id, shipping_address_id, total_price)
            VALUES (?, ?, ?, ?)
            """;
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, new String[]{"order_id_2"});
                ps.setInt(1, customerId);
                ps.setInt(2, cartId);
                ps.setInt(3, shippingAddressId);
                ps.setObject(4, total, Types.NUMERIC);
                return ps;
            }, kh);
            return kh.getKey().intValue();
        }

        // … rest unchanged …
        public void savePayment(int customerId, int orderId, PaymentDto p) {
            String sql = """
                INSERT INTO payment2
                  (customer_id, order_id_2, amount, method)
                VALUES (?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                    customerId,
                    orderId,
                    // you might want to pass real total here instead of 0.0
                    p.getMethod().equals("cod") ? 0.0 : 0.0,
                    p.getMethod()
            );
        }

        /**
         * High-level “place order” method your controller will call.
         */
        public int placeOrder(OrderRequest req) {
            int customerId = req.getCustomerId();  // now non-null, set by controller

            // 1) shipping
            int shipId = saveShippingAddress(customerId, req.getShippingAddress());

            // 2) compute cart total
            Double total = jdbcTemplate.queryForObject(
                    """
                    SELECT SUM(COALESCE(v.variant_price,p.base_price)*cv.quantity)
                    FROM cartvariant cv
                    LEFT JOIN variant v   ON cv.variant_id = v.variant_id
                    LEFT JOIN product p   ON cv.product_id = p.product_id
                    WHERE cv.cart_id = ?
                    """,
                    Double.class,
                    req.getCartId()
            );

            // 3) order row — use the real customerId here, not `1`
            int orderId = saveOrder(customerId, req.getCartId(), shipId, total == null ? 0 : total);

            // 4) payment row
            savePayment(customerId, orderId, req.getPayment());
            return orderId;
        }

        /**
         * 1) Add an item to an *existing* cart (cart_id must already exist).
         *    This replaces the old addToCart(...) method’s cart-creation logic.
         */
        public void addItemToExistingCart(int cartId, Integer productId, Integer variantId, int quantity) {
            String sql = "INSERT INTO cartvariant (cart_id, product_id, variant_id, quantity) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, cartId);
                if (productId != null && productId > 0) ps.setInt(2, productId);
                else ps.setNull(2, Types.INTEGER);
                if (variantId != null) ps.setInt(3, variantId);
                else ps.setNull(3, Types.INTEGER);
                ps.setInt(4, quantity);
                return ps;
            });
        }
        // Optional: adjust your existing addToCart to delegate to addItemToExistingCart if you want
        public int addToCart(int productId, Integer variantId, int quantity) {
            String cartSql = "INSERT INTO cart (quantity) VALUES (?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(cartSql, new String[]{"cart_id"});
                ps.setInt(1, quantity);
                return ps;
            }, kh);
            int newCartId = kh.getKey().intValue();
            addItemToExistingCart(newCartId, productId, variantId, quantity);
            return newCartId;
        }



        public int addToCartByProductId(int productId, int quantity) {
            String sql = "SELECT v.variant_id FROM variant v "
                    + "JOIN productvariant pv ON v.variant_id = pv.variant_id "
                    + "WHERE pv.product_id = ? AND ROWNUM = 1";
            List<Integer> variantIds = jdbcTemplate.query(
                    sql,
                    (rs, i) -> rs.getInt("variant_id"),
                    productId
            );
            return variantIds.isEmpty()
                    ? addToCart(productId, null, quantity)
                    : addToCart(productId, variantIds.get(0), quantity);
        }

        public int addOrUpdateCart(Integer cartId, Integer productId, Integer variantId, int quantity) {
            if (cartId != null) {
                // Append to existing cart
                addItemToExistingCart(cartId, productId, variantId, quantity);
                return cartId;
            } else {
                // Create new cart and add first item
                return addToCart(productId != null ? productId : 0, variantId, quantity);
            }
        }




        public int addOrGetCartAndInsertItem(int customerId,
                                             Integer productId,
                                             Integer variantId,
                                             int quantity) {
            // 1) Look for an existing cart for this customer
            Integer existingCart = findCartIdByCustomer(customerId);

            // 2) Compute the cartId exactly once, and make it final
            final int cartId;
            if (existingCart == null) {
                // no cart yet → create one and tie it to the customer
                cartId = createNewCart(customerId);
                jdbcTemplate.update(
                        "UPDATE cart SET customer_id = ? WHERE cart_id = ?",
                        customerId, cartId
                );
            } else {
                cartId = existingCart;
            }

            // 3) Insert the line item
            String insertSql =
                    "INSERT INTO cartvariant (cart_id, product_id, variant_id, quantity) " +
                            "VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(insertSql);
                ps.setInt(1, cartId);
                if (productId != null && productId > 0) ps.setInt(2, productId);
                else                                  ps.setNull(2, Types.INTEGER);
                if (variantId != null)               ps.setInt(3, variantId);
                else                                  ps.setNull(3, Types.INTEGER);
                ps.setInt(4, quantity);
                return ps;
            });

            return cartId;
        }
        public int createNewCart(int customerId) {
            String sql = "INSERT INTO cart (customer_id, quantity) VALUES (?, 0)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, new String[]{"cart_id"});
                ps.setInt(1, customerId);
                return ps;
            }, kh);
            return kh.getKey().intValue();
        }

        public Integer findCartIdByCustomer(int customerId) {
            String sql = "SELECT cart_id FROM cart WHERE customer_id = ?";
            List<Integer> ids = jdbcTemplate.query(
                    sql,
                    (rs, rn) -> rs.getInt("cart_id"),
                    customerId
            );
            return ids.isEmpty() ? null : ids.get(0);
        }
        public Integer findCustomerIdByUserName(String userName) {
            String sql = "SELECT customer_id FROM customer WHERE user_name = ?";
            List<Integer> ids = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> rs.getInt("customer_id"),
                    userName
            );
            return ids.isEmpty() ? null : ids.get(0);
        }
        /**
         * Update the quantity of a specific cart item.
         */
        public void updateCartQuantity(int cartVariantId, int quantity) {
            String sql = "UPDATE cartvariant SET quantity = ? WHERE cartvariant_id = ?";
            jdbcTemplate.update(sql, quantity, cartVariantId);
        }

        /**
         * Remove a specific item from the cart.
         */
        public void removeFromCart(int cartVariantId) {
            String sql = "DELETE FROM cartvariant WHERE cartvariant_id = ?";
            jdbcTemplate.update(sql, cartVariantId);
        }

        // ... rest of service unchanged ...
    }

