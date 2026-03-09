package com.TroisN.Service.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SuspendUserRequest {

    @NotBlank
    @Email
    private String email;

    // null = suspension définitive
    @Min(1)
    private Integer suspendMinutes;
}
