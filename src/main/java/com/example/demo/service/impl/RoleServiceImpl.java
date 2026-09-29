package com.example.demo.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.IPermissionRepository;
import com.example.demo.repository.IRoleRepository;
import com.example.demo.repository.IUserRepository;
import com.example.demo.service.IRoleService;

@Service
public class RoleServiceImpl implements IRoleService {

    private final IRoleRepository roleRepository;

    private final IPermissionRepository permissionRepository;
    
    private final IUserRepository userRepository;

    public RoleServiceImpl(IRoleRepository roleRepository, IPermissionRepository permissionRepository, IUserRepository userRepository) {
        
        this.roleRepository = roleRepository;
        
        this.permissionRepository = permissionRepository;
        
        this.userRepository = userRepository;
    
    }

    @Override
    public List<Role> findAll() {

        return roleRepository.findAll();

    }

    @Override
    public Role findById(Long id) {

        return roleRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Rol no encontrado con id " + id));

    }

    @Override
    public Role findByName(String name) {

        return roleRepository.findByName(name).orElseThrow(() -> new NoSuchElementException("Rol no encontrado con nombre " + name));
    
    }

    @Override
    @Transactional
    public Role create(Role role) {

        
        validateName(role.getName());
        
        if (roleRepository.existsByName(role.getName())) {
        
            throw new IllegalArgumentException("Ya existe un rol con nombre " + role.getName());
        
        }
        
        role.setId(null);
        
        role.setPermissions(resolvePermissions(role.getPermissions()));
        
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role update(Long id, Role role) {
        
        Role existing = findById(id);
        
        validateName(role.getName());
        
        if (!existing.getName().equals(role.getName()) && roleRepository.existsByName(role.getName())) {
        
            throw new IllegalArgumentException("Ya existe un rol con nombre " + role.getName());
        
        }
        
        existing.setName(role.getName());
        
        existing.setDescription(role.getDescription());
        
        existing.setPermissions(resolvePermissions(role.getPermissions()));
        
        return roleRepository.save(existing);
    
    }

    @Override
    @Transactional
    public void delete(Long id) {
        
        Role role = findById(id);
        
        List<User> users = userRepository.findByRolesId(id);
        
        for (User user : users) {
        
            if (user.getRoles().size() == 1) {
        
                throw new IllegalStateException("No se puede eliminar: el usuario " + user.getUsername() + " quedaria sin roles");
        
            }
        }
        
        for (User user : users) {
        
            user.getRoles().remove(role);
        
            userRepository.save(user);
        
        }
        
        roleRepository.delete(role);
    
    }

    @Override
    @Transactional
    public Role addPermission(Long roleId, Long permissionId) {
        
        Role role = findById(roleId);
        
        Permission permission = findPermission(permissionId);
        
        role.getPermissions().add(permission);
        
        return roleRepository.save(role);
    
    }

    @Override
    @Transactional
    public Role removePermission(Long roleId, Long permissionId) {
    
        Role role = findById(roleId);
    
        Permission permission = findPermission(permissionId);
    
        if (!role.getPermissions().contains(permission)) {
    
            throw new IllegalArgumentException("El rol no tiene el permiso " + permission.getName());
    
        }
    
        if (role.getPermissions().size() == 1) {
    
            throw new IllegalStateException("No se puede quitar el ultimo permiso del rol " + role.getName());
    
        }
    
        role.getPermissions().remove(permission);
    
        return roleRepository.save(role);
    }

    private void validateName(String name) {
      
        if (name == null || name.isBlank()) {
      
            throw new IllegalArgumentException("El nombre del rol es obligatorio");
      
        }
    
    }

    private Set<Permission> resolvePermissions(Set<Permission> permissions) {
    
        if (permissions == null || permissions.isEmpty()) {
    
            throw new IllegalArgumentException("Un rol debe tener al menos un permiso");
    
        }
    
        Set<Permission> resolved = new HashSet<>();
    
        for (Permission p : permissions) {
    
            resolved.add(findPermission(p.getId()));
    
        }
    
        return resolved;
    
    }

    private Permission findPermission(Long permissionId) {
       
        return permissionRepository.findById(permissionId).orElseThrow(() -> new NoSuchElementException("Permiso no encontrado con id " + permissionId));
    
    }
}