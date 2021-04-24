package org.home.syncBox.service;

import org.home.syncBox.model.User;

import java.util.List;

public interface UserService {

    User register(User user);

    User update(User user);

    List<User> getAll();

    List<User> allUsers(int pageNum, int page_size);

    User findByUsername(String username);

    void setLastEnter(Long userId, Long lastEnter);

    User findById(Long id);

    void delete(Long id);
}
