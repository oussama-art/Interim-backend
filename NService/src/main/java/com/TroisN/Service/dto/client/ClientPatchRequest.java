package com.TroisN.Service.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ClientPatchRequest
{



    @Email
    private String emailAddress;

    private String phoneNumber;

    @Min(0)
    private Integer experienceYear;

    private String title;
    private String description;
    private String sector;

    private Integer nbEmployee;
}
