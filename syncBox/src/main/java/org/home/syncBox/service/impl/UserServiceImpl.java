package org.home.syncBox.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.home.syncBox.model.Role;
import org.home.syncBox.model.Status;
import org.home.syncBox.model.User;
import org.home.syncBox.repository.RoleRepository;
import org.home.syncBox.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_USER_ROLE_NAME = "ROLE_USER";

    private final org.home.syncBox.repository.UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    //@Autowired
    public UserServiceImpl(org.home.syncBox.repository.UserRepository userRepository, RoleRepository roleRepository, @Lazy BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(User user) {
        Role roleUser = roleRepository.findByName(DEFAULT_USER_ROLE_NAME);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singletonList(roleUser));
        user.setStatus(Status.ACTIVE);

        User registeredUser = userRepository.save(user);

        log.info("IN register - user: {} successfully registered", registeredUser);

        return registeredUser;
    }

    @Override
    public User update(User user) {
        if (user.getRoles().size() == 0)
            user.setRoles(Arrays.asList(roleRepository.findByName("ROLE_USER")));

        log.info("IN update - user: {} successfully updated", user);

        return userRepository.save(user);
    }


    @Override
    public List<User> getAllUsers(int pageNum, int pageSize) {
        Pageable pageWithSomeElements = PageRequest.of(pageNum, pageSize);
        return userRepository.findAll(pageWithSomeElements).getContent();
    }

    @Override
    public User getByUsername(String username) {
        User result = userRepository.findByUsername(username).orElse(null);
        log.info("IN findByUsername - user: {} find by username: {}", result, username);
        return result;
    }

    @Override
    public void setLastEnter(Long userId, Long lastEnter) {
        userRepository.setLastEnter(userId, lastEnter);
    }

    @Override
    public User getById(Long id) {
        User result = userRepository.findById(id).orElse(null);

        if (result == null) {
            log.warn("IN findById - no user find by id: {}", id);
            return null;
        }

        log.info("IN findById - user: {} find by id: {}", result, id);
        return result;
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
        log.info("IN delete  - user with id: {} successfully deleted", id);
    }
}
