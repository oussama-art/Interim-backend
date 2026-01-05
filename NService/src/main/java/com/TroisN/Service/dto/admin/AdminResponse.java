package com.TroisN.Service.dto.admin;

import com.TroisN.Service.dto.BaseUserResponse;
import com.TroisN.Service.dto.user.LoginResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse extends BaseUserResponse {

    private Integer adminLevel;

    public AdminResponse(
            Long id,
            String firstName,
            String lastName,
            String phoneNumber,
            String emailAddress,
            Integer experienceYear,
            Integer adminLevel
    ) {
        super(id, firstName, lastName, phoneNumber, emailAddress, experienceYear);
        this.adminLevel = adminLevel;
    }
}
