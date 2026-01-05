package com.TroisN.Service.mapper;
import com.TroisN.Service.dto.client.ClientResponse;
import com.TroisN.Service.entity.ClientCompany;

public class ClientMapper {

    public static ClientResponse toResponseDto(ClientCompany clientCompany){
        if (clientCompany == null) return null;

        ClientResponse dto = new ClientResponse();

        dto.setId(clientCompany.getId());

        dto.setPhoneNumber(clientCompany.getPhoneNumber());
        dto.setEmailAddress(clientCompany.getEmailAddress());
        dto.setExperienceYear(clientCompany.getExperienceYear());
        dto.setNbEmployee(clientCompany.getNbEmployee());
        dto.setDescription(clientCompany.getDescription());
        dto.setTitle(clientCompany.getTitle());
        dto.setSector(clientCompany.getSector());

        return dto;

    }
}
