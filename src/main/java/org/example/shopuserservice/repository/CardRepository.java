package org.example.shopuserservice.repository;


import org.example.shopuserservice.model.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    @Modifying
    @Query("UPDATE Card c SET c.active = :active WHERE c.id = :id")
    void setActiveById(@Param("id") Long id, @Param("active") boolean active);

    Long countByUserId(Long userId);

}
