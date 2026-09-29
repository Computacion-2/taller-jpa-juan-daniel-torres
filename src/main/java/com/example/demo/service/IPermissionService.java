package com.example.demo.service;

import java.util.List;
import com.example.demo.model.Permission;

public interface IPermissionService {

    List<Permission> findAll();

    Permission findById(Long id);

    Permission findByName(String name);

    Permission create(Permission permission);

    Permission update(Long id, Permission permission);

    void delete(Long id);
    
}