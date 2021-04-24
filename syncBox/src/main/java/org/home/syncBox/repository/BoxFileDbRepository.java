package org.home.syncBox.repository;

import org.home.syncBox.model.BoxFile;
import org.home.syncBox.model.Status;
import org.home.syncBox.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public
interface BoxFileDbRepository extends JpaRepository<BoxFile, Long> {

    Optional<BoxFile> getBoxFileByName(String s);

    @Query("select bf from BoxFile bf WHERE bf.user = :user and bf.status = :status")
    List<BoxFile> getBoxFileByUsernameAndStatusPageable(@Param("user") User user, @Param("status") Status status, Pageable pageable);
}
