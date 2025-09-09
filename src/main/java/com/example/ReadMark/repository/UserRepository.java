package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
}

