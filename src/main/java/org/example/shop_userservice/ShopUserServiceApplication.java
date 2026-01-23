package org.example.shop_userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
@EnableCaching
public class ShopUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopUserServiceApplication.class, args);
    }

}
