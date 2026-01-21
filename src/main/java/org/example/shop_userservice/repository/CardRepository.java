package org.example.shop_userservice.repository;


import org.example.shop_userservice.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE Card c SET c.active = :active WHERE c.id = :id")
    void setActiveById(@Param("id") Long id, @Param("active") boolean active);
}
