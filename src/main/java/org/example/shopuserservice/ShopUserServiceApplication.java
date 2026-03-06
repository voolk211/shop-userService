package org.example.shopuserservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaAuditing
@EnableMethodSecurity
@EnableTransactionManagement
@EnableCaching
public class ShopUserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopUserServiceApplication.class, args);
    }
}
