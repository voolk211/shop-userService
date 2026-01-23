package org.example.shop_userservice.model.patchDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserPatchDto {

    @NotNull
    private Boolean active;

}
