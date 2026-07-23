package com.example.userservice.repository;

import com.example.userservice.entity.PayCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentCardRepository extends JpaRepository<PayCard, Long>,
        JpaSpecificationExecutor<PayCard> {

    List<PayCard> findByUserId(Long userId);

    @Query("SELECT COUNT(c) FROM PayCard c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM payment_cards WHERE user_id = :userId AND active = true",
            nativeQuery = true)
    List<PayCard> findActiveCardsByUserId(@Param("userId") Long userId);
}