package org.example.shopuserservice.model.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;

@Data
public class UserDto {

    private Long id;

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

    @NotNull(message = "Active must not be null")
    private Boolean active;

}
