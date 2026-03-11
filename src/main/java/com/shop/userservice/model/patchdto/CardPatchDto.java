package com.shop.userservice.model.patchdto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardPatchDto {

    @NotNull
    private Boolean active;

}
