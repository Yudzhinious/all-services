package com.example.userservice.repository;

import com.example.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.name = :name AND u.surname = :surname")
    Page<User> findByNameAndSurname(@Param("name") String name,
                                    @Param("surname") String surname,
                                    Pageable pageable);

    @Query(value = "SELECT * FROM users u WHERE u.active = true", nativeQuery = true)
    Page<User> findAllActiveUsers(Pageable pageable);
}