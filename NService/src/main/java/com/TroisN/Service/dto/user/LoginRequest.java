package com.TroisN.Service.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    @Email
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    protected String emailAddress;

    @NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    protected String password;
}
