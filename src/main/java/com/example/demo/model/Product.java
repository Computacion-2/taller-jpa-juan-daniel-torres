package com.example.demo.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price; 
    
    @Column(nullable = false)
    private Integer stock; 
    
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
