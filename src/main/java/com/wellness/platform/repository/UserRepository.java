package com.wellness.platform.repository;

import com.wellness.platform.model.Role;
import com.wellness.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.name) = LOWER(:name)")
    Optional<User> findByName(@Param("name") String name);

    Boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.name) = LOWER(:name)")
    Boolean existsByName(@Param("name") String name);

    List<User> findByRole(Role role);
}
