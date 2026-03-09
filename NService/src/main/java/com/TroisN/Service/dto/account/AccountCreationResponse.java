package com.TroisN.Service.dto.account;

import com.TroisN.Service.enums.RequestStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class AccountCreationResponse {

    private Long id;

    private String firstName;
    private String lastName;
    private String emailAddress;

    private List<AccountEmailResponse> emails;

    private String phoneNumber;
    private String companyTitle;
    private Integer experienceYear;

    private String sector;
    private Integer nbEmployee;

    private Integer requestedAccounts;
    private RequestStatus status;

    private LocalDateTime createdAt;
}


