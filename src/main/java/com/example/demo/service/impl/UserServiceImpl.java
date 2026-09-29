package com.example.demo.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.IOrderRepository;
import com.example.demo.repository.IRoleRepository;
import com.example.demo.repository.IUserRepository;
import com.example.demo.service.IUserService;

@Service
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    
    private final IRoleRepository roleRepository;
    
    private final IOrderRepository orderRepository;

    public UserServiceImpl(IUserRepository userRepository, IRoleRepository roleRepository,IOrderRepository orderRepository) {
        
        this.userRepository = userRepository;
        
        this.roleRepository = roleRepository;
        
        this.orderRepository = orderRepository;
    
    }

    @Override
    public List<User> findAll() {
    
        return userRepository.findAll();
    
    }

    @Override
    public User findById(Long id) {
    
        return userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con id " + id));
    
    }

    @Override
    public User findByUsername(String username) {
    
        return userRepository.findByUsername(username).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con username " + username));
    
    }

    @Override
    @Transactional
    public User create(User user) {
    
        validateRequired(user.getUsername(), "username");
    
        validateRequired(user.getEmail(), "email");
    
        validateRequired(user.getPassword(), "password");
    
        if (userRepository.existsByUsername(user.getUsername())) {
    
            throw new IllegalArgumentException("Ya existe un usuario con username " + user.getUsername());
    
        }
    
        if (userRepository.existsByEmail(user.getEmail())) {
    
            throw new IllegalArgumentException("Ya existe un usuario con email " + user.getEmail());
    
        }
    
        user.setId(null);
    
        user.setRoles(resolveRoles(user.getRoles()));
    
        return userRepository.save(user);
    
    }

    @Override
    @Transactional
    public User update(Long id, User user) {
    
        User existing = findById(id);
    
        validateRequired(user.getUsername(), "username");
    
        validateRequired(user.getEmail(), "email");
    
        if (!existing.getUsername().equals(user.getUsername())&& userRepository.existsByUsername(user.getUsername())) {
            
            throw new IllegalArgumentException("Ya existe un usuario con username " + user.getUsername());
        
        }
        
        if (!existing.getEmail().equals(user.getEmail())&& userRepository.existsByEmail(user.getEmail())) {
            
            throw new IllegalArgumentException("Ya existe un usuario con email " + user.getEmail());
        
        }
        
        existing.setUsername(user.getUsername());
        
        existing.setEmail(user.getEmail());
        
        existing.setFullName(user.getFullName());
        
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
        
            existing.setPassword(user.getPassword());
        
        }
        
        existing.setRoles(resolveRoles(user.getRoles()));
        
        return userRepository.save(existing);
    
    }

    @Override
    @Transactional
    public void delete(Long id) {
    
        User user = findById(id);
    
        if (!orderRepository.findByUserId(id).isEmpty()) {
    
            throw new IllegalStateException("No se puede eliminar: el usuario " + user.getUsername() + " tiene pedidos registrados");
        
        }
        
        userRepository.delete(user);
    
    }

    @Override
    @Transactional
    public User addRole(Long userId, Long roleId) {
    
        User user = findById(userId);
    
        Role role = findRole(roleId);
    
        user.getRoles().add(role);
    
        return userRepository.save(user);
    
    }

    @Override
    @Transactional
    public User removeRole(Long userId, Long roleId) {
    
        User user = findById(userId);
    
        Role role = findRole(roleId);
    
        if (!user.getRoles().contains(role)) {
    
            throw new IllegalArgumentException("El usuario no tiene el rol " + role.getName());
    
        }
    
        if (user.getRoles().size() == 1) {
    
            throw new IllegalStateException("No se puede quitar el ultimo rol del usuario " + user.getUsername());
    
        }
    
        user.getRoles().remove(role);
    
        return userRepository.save(user);
    
    }

    private void validateRequired(String value, String field) {
    
        if (value == null || value.isBlank()) {
    
            throw new IllegalArgumentException("El campo " + field + " es obligatorio");
    
        }
    
    }

    private Set<Role> resolveRoles(Set<Role> roles) {
    
        if (roles == null || roles.isEmpty()) {
    
            throw new IllegalArgumentException("Un usuario debe tener al menos un rol");
    
        }
    
        Set<Role> resolved = new HashSet<>();
    
        for (Role r : roles) {
    
            resolved.add(findRole(r.getId()));
    
        }
    
        return resolved;
    
    }

    private Role findRole(Long roleId) {
    
        return roleRepository.findById(roleId) .orElseThrow(() -> new NoSuchElementException("Rol no encontrado con id " + roleId));
    
    }
}