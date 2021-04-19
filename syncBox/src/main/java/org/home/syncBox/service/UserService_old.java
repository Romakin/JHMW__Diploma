package org.home.syncBox.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.home.syncBox.entity.Role;
import org.home.syncBox.entity.User;
import org.home.syncBox.repository.RoleRepository_old;
import org.home.syncBox.repository.UserRepository_old;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class UserService_old implements UserDetailsService {

    @Autowired
    private UserRepository_old userRepositoryOld;

    @Autowired
    private RoleRepository_old roleRepository;

    private BCryptPasswordEncoder passwordEncoder;

    private final int PAGING_SIZE = 20;


    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> user = userRepositoryOld.findByUsername(login);
        if (user.isPresent())
            throw new UsernameNotFoundException("Invalid login");
        return user.get();
    }

    public User findUserById(Long id) {
        Optional<User> user = userRepositoryOld.findById(id);
        return user.orElse(new User());
    }

    public List<User> allUsers() {
        return userRepositoryOld.findAll();
    }

    public List<User> allUsers(int pageNum) {
        Pageable pageWithSomeElements = PageRequest.of(pageNum, PAGING_SIZE);
        return userRepositoryOld.findAll(pageWithSomeElements).getContent();
    }

    public boolean saveUser(User user) {
        Optional<User> userFromDB = userRepositoryOld.findByUsername(user.getUsername());
        if (!userFromDB.isPresent()) return false;
        user.setRoles(Collections.singleton(new Role(1L, "ROLE_USER")));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepositoryOld.save(user);
        return true;
    }

    public boolean deleteUser(Long userId) {
        if (userRepositoryOld.findById(userId).isPresent()) {
            userRepositoryOld.deleteById(userId);
            return true;
        }
        return false;
    }

}
