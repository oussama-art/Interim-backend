package com.TroisN.Service.mapper;

import com.TroisN.Service.dto.account.AccountCreationRequest;
import com.TroisN.Service.dto.account.AccountCreationResponse;
import com.TroisN.Service.dto.account.AccountEmailResponse;
import com.TroisN.Service.entity.Account;
import com.TroisN.Service.entity.AccountEmail;
import com.TroisN.Service.enums.RequestStatus;

import java.util.List;
import java.util.stream.Collectors;

public class AccountCreationMapper {


    public static Account toEntity(AccountCreationRequest dto) {

        if (dto == null) {
            return null;
        }

        Account entity = new Account();


        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmailAddress(dto.getEmailAddress());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setExperienceYear(dto.getExperienceYear());


        entity.setCompanyTitle(dto.getCompanyTitle());
        entity.setCompanyDescription(dto.getCompanyDescription());
        entity.setSector(dto.getSector());
        entity.setNbEmployee(dto.getNbEmployee());

        // Demande
        entity.setRequestedAccounts(dto.getRequestedAccounts());

        List<AccountEmail> emails = dto.getAdditionalEmails()
                .stream()
                .map(email -> {
                    AccountEmail ae = new AccountEmail();
                    ae.setEmail(email);
                    ae.setStatus(RequestStatus.PENDING);
                    return ae;
                })
                .collect(Collectors.toList());

        entity.setEmails(emails);


        return entity;
    }


    public static AccountCreationResponse toResponse(Account entity) {

        if (entity == null) {
            return null;
        }

        AccountCreationResponse response = new AccountCreationResponse();

        response.setId(entity.getId());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setEmailAddress(entity.getEmailAddress());

        response.setPhoneNumber(entity.getPhoneNumber());
        response.setExperienceYear(entity.getExperienceYear());
        response.setCompanyTitle(entity.getCompanyTitle());
        response.setSector(entity.getSector());
        response.setNbEmployee(entity.getNbEmployee());

        response.setRequestedAccounts(entity.getRequestedAccounts());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());

        // ⭐ mapping des emails avec status
        response.setEmails(
                entity.getEmails().stream()
                        .map(email -> {
                            AccountEmailResponse dto = new AccountEmailResponse();
                            dto.setEmail(email.getEmail());
                            dto.setStatus(email.getStatus());
                            return dto;
                        })
                        .toList()
        );

        return response;
    }

}
