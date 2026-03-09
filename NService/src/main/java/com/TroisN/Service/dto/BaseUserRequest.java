package com.TroisN.Service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseUserRequest {


    private String firstName;


    private String lastName;

    @Pattern(
            regexp = "^\\+[1-9]\\d{1,2}\\d{6,12}$",
            message = "Numéro invalide (format invalide)"
    )
    private String phoneNumber;

    @NotBlank
    @Email
    private String emailAddress;

    @Min(0)
    private Integer experienceYear;

    @NotBlank
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotBlank
    private String confirmPassword;
}
