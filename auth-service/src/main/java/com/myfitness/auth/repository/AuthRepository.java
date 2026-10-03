package com.myfitness.auth.repository;

import com.myfitness.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
}
