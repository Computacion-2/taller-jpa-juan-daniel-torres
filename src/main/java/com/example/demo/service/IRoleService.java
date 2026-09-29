package com.example.demo.service;

import java.util.List;
import com.example.demo.model.Role;

public interface IRoleService {

    List<Role> findAll();

    Role findById(Long id);

    Role findByName(String name);

    Role create(Role role);

    Role update(Long id, Role role);

    void delete(Long id);

    Role addPermission(Long roleId, Long permissionId);

    Role removePermission(Long roleId, Long permissionId);
    
}