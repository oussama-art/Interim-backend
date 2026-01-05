package com.TroisN.Service.dto.demande;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DemandeRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    @Min(1)
    private Integer totalEmployeesNeeded;

    @NotNull
    private List<DemandeProfilRequest> profils = new ArrayList<>();;
}
