package com.example.demo.model;

import java.util.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data 
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_permissions",joinColumns = @JoinColumn(name = "role_id"),inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Permission> permissions = new HashSet<>();
    
}
