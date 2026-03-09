package com.TroisN.Service.dto.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AccountApprovalRequest {

    @NotEmpty(message = "La liste des emails sélectionnés est obligatoire")
    private List<@Email String> selectedEmails;
}
