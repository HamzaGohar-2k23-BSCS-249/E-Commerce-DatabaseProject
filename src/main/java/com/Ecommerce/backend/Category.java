package com.Ecommerce.backend;

import jakarta.persistence.*;

@Entity
public class Category {
    @Id
    @Column(name = "category_id")
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "category_seq_gen"
    )
    @SequenceGenerator(
            name = "category_seq_gen",
            sequenceName = "seq_category",
            allocationSize = 1
    )
    Integer categoryId;
    String name;

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
