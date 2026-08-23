package com.servicedesk.service;

import com.servicedesk.domain.User;
import com.servicedesk.domain.enums.Role;
import com.servicedesk.exception.ResourceNotFoundException;
import com.servicedesk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findByActiveTrue();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRoleAndActiveTrue(role);
    }

    public List<User> getAssignableUsers() {
        return userRepository.findAssignableUsers();
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
