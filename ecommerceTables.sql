CREATE TABLE customer (Customer_ID NUMBER , Email VARCHAR2(60) NOT NULL UNIQUE, Password VARCHAR2(30) NOT NULL, Date_Of_Birth Date NOT NULL, Gender VARCHAR2(10), First_Name VARCHAR2(50) NOT NULL, Last_Name VARCHAR(50) NOT NULL,user_name VARCHAR2(60) NOT NULL UNIQUE, CONSTRAINT pk_customer_id PRIMARY KEY(Customer_ID))
/
CREATE SEQUENCE seq_customer START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_customer_bi BEFORE INSERT ON customer FOR EACH ROW BEGIN IF :NEW.customer_id IS NULL THEN SELECT seq_customer.NEXTVAL INTO :NEW.customer_id FROM dual;END IF; END;
/
CREATE TABLE category(category_id NUMBER , category_name VARCHAR2(20) NOT NULL , CONSTRAINT pk_category_id PRIMARY KEY(category_id))
/
 CREATE TABLE product (product_id NUMBER,  Category_id NUMBER NOT NULL, Main_image VARCHAR2(200) NOT NULL, Name VARCHAR2(30) NOT NULL, base_price NUMBER(10,2) NOT NULL, Cost NUMBER(10,2) NOT NULL, Description VARCHAR2(4000) NOT NULL, Weight NUMBER(10,2) NOT NULL, Created_At DATE DEFAULT SYSDATE NOT NULL, Modified_at DATE DEFAULT SYSDATE NOT NULL, Manufacturer VARCHAR2(70) NOT NULL, CONSTRAINT pk_product_id PRIMARY KEY (product_id) , CONSTRAINT fk_category_id FOREIGN KEY(category_id) REFERENCES category (category_id))
/
CREATE SEQUENCE seq_product START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_product_bi BEFORE INSERT ON product FOR EACH ROW BEGIN IF :NEW.product_id IS NULL THEN SELECT seq_product.NEXTVAL INTO :NEW.product_id FROM dual;END IF; END;
/
CREATE TABLE product_image(product_image_id NUMBER,product_id NUMBER NOT NULL,  image_url VARCHAR2(60) NOT NULL, uploaded_at DATE DEFAULT SYSDATE ,CONSTRAINT pk_product_image PRIMARY KEY(product_image_id), CONSTRAINT fk_product_id_pi FOREIGN KEY (product_id) REFERENCES product(product_id))
/
CREATE TABLE inventory(inventory_id NUMBER, product_id NUMBER NOT NULL, Quantity NUMBER NOT NULL, Last_updated DATE DEFAULT SYSDATE NOT NULL, CONSTRAINT pk_inventory_id PRIMARY KEY(inventory_id), CONSTRAINT fk_product_id_inventory FOREIGN KEY(product_id)REFERENCES product (product_id))
/
CREATE TABLE variant (variant_id NUMBER,  Color VARCHAR2(60) NOT NULL, variant_weight NUMBER NOT NULL ,siz VARCHAR2(30) NOT NULL,  variant_price NUMBER NOT NULL, CONSTRAINT pk_variant_id PRIMARY KEY(variant_id))
/
CREATE TABLE productvariant(productvariant_id NUMBER, product_id NUMBER NOT NULL, variant_id NUMBER NOT NULL, CONSTRAINT pk_productvariant_id PRIMARY KEY (productvariant_id), CONSTRAINT fk_product_id_pv FOREIGN KEY (product_id) REFERENCES product(product_id), CONSTRAINT fk_variant_id_pv FOREIGN KEY(variant_id) REFERENCES variant(variant_id))
/
CREATE TABLE cart(cart_id NUMBER ,customer_id NUMBER, quantity NUMBER NOT NULL, CONSTRAINT pk_cart_id PRIMARY KEY(cart_id),CONSTRAINT fk_cart_id_cart FOREIGN KEY (customer_id) REFERENCES customer (customer_id))
/
CREATE TABLE cartvariant (cartvariant_id NUMBER PRIMARY KEY, cart_id NUMBER NOT NULL, product_id NUMBER NULL, variant_id NUMBER NULL, quantity NUMBER DEFAULT 1 NOT NULL, CONSTRAINT fk_cart_id_cv FOREIGN KEY (cart_id) REFERENCES cart(cart_id), CONSTRAINT fk_variant_id_cv FOREIGN KEY (variant_id) REFERENCES variant(variant_id), CONSTRAINT fk_product_id_cv FOREIGN KEY (product_id) REFERENCES product(product_id), CONSTRAINT ck_cartvariant_item CHECK (variant_id IS NOT NULL OR product_id IS NOT NULL));

CREATE TABLE customerproduct(customerproduct_id NUMBER, customer_id NUMBER NOT NULL, product_id NUMBER NOT NULL, CONSTRAINT pk_customerproduct_id PRIMARY KEY (customerproduct_id),CONSTRAINT fk_customer_id_cp FOREIGN KEY(customer_id) REFERENCES customer (customer_id), CONSTRAINT fk_product_id_cp FOREIGN KEY(product_id) REFERENCES product(product_id));
/
CREATE TABLE review (review_id NUMBER, customer_id NUMBER NOT NULL, product_id NUMBER, ratings NUMBER NOT NULL, title VARCHAR2(100) NOT NULL, text VARCHAR2(4000) NOT NULL, created_at DATE DEFAULT SYSDATE NOT NULL, CONSTRAINT pk_review_id PRIMARY KEY(review_id), CONSTRAINT fk_customer_id_review FOREIGN KEY(customer_id) REFERENCES customer (customer_id), CONSTRAINT fk_product_id_review FOREIGN KEY(product_id) REFERENCES product (product_id))
/
CREATE TABLE shippingAddress (shipping_address_id NUMBER, customer_id NUMBER NOT NULL, first_name VARCHAR2(30) NOT NULL, last_name VARCHAR2 (30) NOT NULL, phone_number NUMBER, address VARCHAR2(60) NOT NULL, province VARCHAR2(100) NOT NULL, city VARCHAR2(30) NOT NULL, area VARCHAR2(60) NOT NULL,CONSTRAINT pk_shipping_address PRIMARY KEY(shipping_address_id), CONSTRAINT fk_shipping_address FOREIGN KEY(customer_id) REFERENCES customer (customer_id))
/
CREATE TABLE order2(order_id_2 NUMBER, customer_id NUMBER NOT NULL, cart_id NUMBER  , shipping_address_id NUMBER NOT NULL, order_date DATE DEFAULT SYSDATE,  total_price NUMBER NOT NULL, order_status VARCHAR2(20) DEFAULT 'pending' NOT NULL, CONSTRAINT pk_order_id_2 PRIMARY KEY(order_id_2), CONSTRAINT fk_customer_id_order FOREIGN KEY(customer_id) REFERENCES customer (customer_id) , CONSTRAINT fk_cart_id_order FOREIGN KEY(cart_id) REFERENCES cart(cart_id), CONSTRAINT fk_shippingaddress_id_order FOREIGN KEY(shipping_address_id) REFERENCES shippingaddress (shipping_address_id))
/
create table payment2(payment_id_2 NUMBER , customer_id NUMBER NOT NULL, order_id_2 NUMBER NOT NULL, method VARCHAR2(30) NOT NULL, created_at DATE DEFAULT SYSDATE NOT NULL,CONSTRAINT pk_payment_id_2 PRIMARY KEY(payment_id_2), CONSTRAINT fk_customer_id_payment2 FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ,CONSTRAINT fk_order_id_payment_2 FOREIGN KEY (order_id_2) REFERENCES order2 (order_id_2))
/
CREATE SEQUENCE seq_category START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_category_bi BEFORE INSERT ON category FOR EACH ROW BEGIN IF :NEW.category_id IS NULL THEN SELECT seq_category.NEXTVAL INTO :NEW.category_id FROM dual; END IF; END;
/
CREATE SEQUENCE seq_product_image START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_product_image_bi BEFORE INSERT ON product_image FOR EACH ROW BEGIN IF :NEW.product_image_id IS NULL THEN SELECT seq_product_image.NEXTVAL INTO :NEW.product_image_id FROM dual; END IF; END;
/
CREATE SEQUENCE seq_inventory START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_inventory_bi BEFORE INSERT ON inventory FOR EACH ROW BEGIN IF :NEW.inventory_id IS NULL THEN SELECT seq_inventory.NEXTVAL INTO :NEW.inventory_id FROM dual; END IF; END;
/
CREATE SEQUENCE seq_variant START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_variant_bi BEFORE INSERT ON variant FOR EACH ROW BEGIN IF :NEW.variant_id IS NULL THEN SELECT seq_variant.NEXTVAL INTO :NEW.variant_id FROM dual; END IF; END;
/
CREATE SEQUENCE seq_productvariant START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_productvariant_bi BEFORE INSERT ON productvariant FOR EACH ROW BEGIN IF :NEW.productvariant_id IS NULL THEN SELECT seq_productvariant.NEXTVAL INTO :NEW.productvariant_id FROM dual; END IF; END;
/
CREATE SEQUENCE seq_cart START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_cart_bi BEFORE INSERT ON cart FOR EACH ROW BEGIN IF :NEW.cart_id IS NULL THEN SELECT seq_cart.NEXTVAL INTO :NEW.cart_id FROM dual; END IF; END;
/
CREATE SEQUENCE seq_cartvariant START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_cartvariant_bi BEFORE INSERT ON cartvariant FOR EACH ROW BEGIN IF :NEW.cartvariant_id IS NULL THEN SELECT seq_cartvariant.NEXTVAL INTO :NEW.cartvariant_id FROM dual; END IF; END;
/
CREATE SEQUENCE seq_customerproduct START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_customerproduct_bi BEFORE INSERT ON customerproduct FOR EACH ROW BEGIN IF :NEW.customerproduct_id IS NULL THEN SELECT seq_customerproduct.NEXTVAL INTO :NEW.customerproduct_id FROM dual; END IF; END;
/
CREATE SEQUENCE seq_review START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_review_bi BEFORE INSERT ON review FOR EACH ROW BEGIN IF :NEW.review_id IS NULL THEN SELECT seq_review.NEXTVAL INTO :NEW.review_id FROM dual; END IF; END;
/
CREATE SEQUENCE seq_shipping_address START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_shipping_address_bi BEFORE INSERT ON shippingAddress FOR EACH ROW BEGIN IF :NEW.shipping_address_id IS NULL THEN SELECT seq_shipping_address.NEXTVAL INTO :NEW.shipping_address_id FROM dual; END IF; END;
/
CREATE SEQUENCE seq_order2 START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_order2_bi BEFORE INSERT ON order2 FOR EACH ROW BEGIN IF :NEW.order_id_2 IS NULL THEN SELECT seq_order2.NEXTVAL INTO :NEW.order_id_2 FROM dual; END IF; END;
/
CREATE SEQUENCE seq_payment2 START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
/
CREATE OR REPLACE TRIGGER trg_payment2_bi BEFORE INSERT ON payment2 FOR EACH ROW BEGIN IF :NEW.payment_id_2 IS NULL THEN SELECT seq_payment2.NEXTVAL INTO :NEW.payment_id_2 FROM dual; END IF; END;
/
