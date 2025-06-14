package com.library.management.service;

import com.library.management.dto.UserDTO;
import com.library.management.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserDTO createUser(UserDTO userDTO);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    User getUserById(Long id);
    User getUserByEmail(String email);
    void changePassword(Long id, String oldPassword, String newPassword);
    void addRole(Long id, String role);
    void removeRole(Long id, String role);
} 