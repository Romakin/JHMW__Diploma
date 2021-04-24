package org.home.syncBox.repository;

import org.home.syncBox.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String name);

    @Transactional
    @Modifying
    @Query("update User u set u.lastEnter = :lastEnter WHERE u.id = :userId")
    void setLastEnter(@Param("userId") Long userId, @Param("lastEnter") Long lastEnter);
}
