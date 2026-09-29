package com.example.demo.service;

import java.util.List;
import com.example.demo.model.User;

public interface IUserService {

    List<User> findAll();

    User findById(Long id);

    User findByUsername(String username);

    User create(User user);

    User update(Long id, User user);

    void delete(Long id);

    User addRole(Long userId, Long roleId);

    User removeRole(Long userId, Long roleId);
    
}