package com.TroisN.Service.dto.demande;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DemandeProfilRequest {

    @NotBlank
    private String profilName;

    @Min(1)
    private Integer quantity;
}
