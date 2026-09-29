package com.example.demo.service.impl;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.repository.IPermissionRepository;
import com.example.demo.repository.IRoleRepository;
import com.example.demo.service.IPermissionService;

@Service
public class PermissionServiceImpl implements IPermissionService {

    private final IPermissionRepository permissionRepository;

    private final IRoleRepository roleRepository;

    public PermissionServiceImpl(IPermissionRepository permissionRepository, IRoleRepository roleRepository) {
        
        this.permissionRepository = permissionRepository;
        
        this.roleRepository = roleRepository;
    
    }

    @Override
    public List<Permission> findAll() {
    
        return permissionRepository.findAll();
    
    }

    @Override
    public Permission findById(Long id) {
    
        return permissionRepository.findById(id) .orElseThrow(() -> new NoSuchElementException("Permiso no encontrado con id " + id));
    
    }

    @Override
    public Permission findByName(String name) {

        return permissionRepository.findByName(name).orElseThrow(() -> new NoSuchElementException("Permiso no encontrado con nombre " + name));
    
    }

    @Override
    public Permission create(Permission permission) {
       
        if (permission.getName() == null || permission.getName().isBlank()) {
       
            throw new IllegalArgumentException("El nombre del permiso es obligatorio");
       
        }
       
        if (permissionRepository.existsByName(permission.getName())) {
       
            throw new IllegalArgumentException("Ya existe un permiso con nombre " + permission.getName());
       
        }
       
        permission.setId(null);
       
        return permissionRepository.save(permission);
    }


    @Override
    public Permission update(Long id, Permission permission) {
    
        Permission existing = findById(id);
    
        if (permission.getName() == null || permission.getName().isBlank()) {
    
            throw new IllegalArgumentException("El nombre del permiso es obligatorio");
    
        }
    
        if (!existing.getName().equals(permission.getName())
    
            && permissionRepository.existsByName(permission.getName())) {throw new IllegalArgumentException("Ya existe un permiso con nombre " + permission.getName());
        
        }
        
        existing.setName(permission.getName());
        
        existing.setDescription(permission.getDescription());
        
        return permissionRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        
        Permission permission = findById(id);
        
        List<Role> roles = roleRepository.findByPermissionsId(id);
        
        for (Role role : roles) {
        
            if (role.getPermissions().size() == 1) {
        
                throw new IllegalStateException("No se puede eliminar: el rol " + role.getName() + " quedaria sin permisos");
            
            }
        
        }
        
        for (Role role : roles) {
        
            role.getPermissions().remove(permission);
        
            roleRepository.save(role);
        }
        
        permissionRepository.delete(permission);
    
    }
}