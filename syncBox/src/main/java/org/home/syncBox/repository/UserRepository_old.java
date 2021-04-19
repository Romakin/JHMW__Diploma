package org.home.syncBox.repository;

import org.home.syncBox.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository_old extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String login);

}
