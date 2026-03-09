package com.TroisN.Service.dto.client;

import com.TroisN.Service.dto.BaseUserRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClientCreateRequest extends BaseUserRequest {

    @NotNull
    private String title;


    private String description;


    private String sector;

    private Integer numberOfAccounts;


    @Min(0)
    private Integer nbEmployee;

    private Long NumDemande;

}
