package com.TroisN.Service.dto.demande;

import lombok.Data;

import java.util.List;

@Data
public class DemandeResponse {

    private Long id;
    private String title;
    private String description;
    private Integer totalEmployeesNeeded;

    private Long clientId;

    private List<DemandeProfilResponse> profils;
}
