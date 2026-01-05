package com.TroisN.Service.dto.candidate;

import com.TroisN.Service.dto.BaseUserResponse;
import com.TroisN.Service.dto.user.LoginResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse extends BaseUserResponse {

    private String skills;
    private String professional;
    private String cin;
    private String cssNumber;
    private String cvPath;

    public CandidateResponse(
            Long id,
            String firstName,
            String lastName,
            String phoneNumber,
            String emailAddress,
            Integer experienceYear,
            String skills,
            String professional,
            String cin,
            String cssNumber,
            String cvPath
    ) {
        super(id, firstName, lastName, phoneNumber, emailAddress, experienceYear);
        this.skills = skills;
        this.professional = professional;
        this.cin = cin;
        this.cssNumber = cssNumber;
        this.cvPath = cvPath;
    }
}
