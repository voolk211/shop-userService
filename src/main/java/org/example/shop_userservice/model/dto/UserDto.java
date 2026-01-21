package org.example.shop_userservice.model.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import org.example.shop_userservice.model.entities.Card;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.validator.constraints.Length;

@Data
public class UserDto {

    @NotNull(message = "Name must not be null")
    @Length(max = 255, message = "Name length must not exceed 255 characters")
    private String name;

    @NotNull(message = "Surname must not be null")
    @Length(max = 255, message = "Surname length must not exceed 255 characters")
    private String surname;

    @NotNull(message = "BirthDate must not be null")
    private LocalDate birthDate;


    @NotNull(message = "Email must not be null")
    @Length(max = 255, message = "Email length must not exceed 255 characters")
    @Email
    private String email;
}
