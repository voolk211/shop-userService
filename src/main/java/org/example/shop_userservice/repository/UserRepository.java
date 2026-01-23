package org.example.shop_userservice.repository;

import org.example.shop_userservice.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Modifying
    @Query(value = "UPDATE users SET active = :active WHERE id = :id", nativeQuery = true)
    void setActiveById(@Param("id") Long id, @Param("active") boolean active);

    boolean existsByEmail(String email);
}
