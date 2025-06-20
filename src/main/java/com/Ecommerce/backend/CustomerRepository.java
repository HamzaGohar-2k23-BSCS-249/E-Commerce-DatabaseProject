package com.Ecommerce.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository <Customer, Integer> {
    Optional<Customer> findByUserName(String userName);

}
