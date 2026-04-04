package com.emailclassifier.repository;

import com.emailclassifier.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //Forces the caller to handle "not found" — no NullPointerException
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}