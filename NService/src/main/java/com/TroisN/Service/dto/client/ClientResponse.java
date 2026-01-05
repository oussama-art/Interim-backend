package com.TroisN.Service.dto.client;

import com.TroisN.Service.dto.BaseUserResponse;
import com.TroisN.Service.dto.user.LoginResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse extends BaseUserResponse {

    private String title;
    private String description;
    private String sector;
    private Integer nbEmployee;

    public ClientResponse(
            Long id,
            String firstName,
            String lastName,
            String phoneNumber,
            String emailAddress,
            Integer experienceYear,
            String title,
            String description,
            String sector,
            Integer nbEmployee
    ) {
        super(id, firstName, lastName, phoneNumber, emailAddress, experienceYear);
        this.title = title;
        this.description = description;
        this.sector = sector;
        this.nbEmployee = nbEmployee;
    }
}

