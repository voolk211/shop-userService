package org.example.shop_userservice.model.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.example.shop_userservice.model.entities.Auditable;
import org.example.shop_userservice.model.entities.User;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class CardDto {

        private Long id;

        @NotNull(message = "Card number must not be null")
        @Pattern(regexp = "\\d{13,19}", message = "Number length must contain 13 to 19 characters")
        private String number;

        @NotNull(message = "Cardholder must not be null")
        @Length(max = 255, message = "Holder length must not exceed 255 characters")
        private String holder;

        @NotNull(message = "Expiration date must not be null")
        @DateTimeFormat
        private LocalDate expirationDate;

        @NotNull(message = "User ID must not be null")
        private Long userId;

        @NotNull(message = "Active must not be null")
        private Boolean active;

}
