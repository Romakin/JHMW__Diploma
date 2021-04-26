package org.home.syncBox.service;

import org.home.syncBox.model.User;

import java.util.List;

public interface UserService {

    User register(User user);

    User update(User user);

    List<User> getAllUsers(int pageNum, int pageSize);

    User getByUsername(String username);

    void setLastEnter(Long userId, Long lastEnter);

    User getById(Long id);

    void delete(Long id);
}
