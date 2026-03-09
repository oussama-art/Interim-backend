package com.TroisN.Service.mapper;

import com.TroisN.Service.dto.admin.AdminResponse;
import com.TroisN.Service.entity.Admin;


public class AdminMapper {

    public static AdminResponse toResponseDTO(Admin admin) {

        if (admin == null) return null;

        AdminResponse dto = new AdminResponse();

        dto.setId(admin.getId());
        dto.setFirstName(admin.getFirstName());
        dto.setLastName(admin.getLastName());
        dto.setPhoneNumber(admin.getPhoneNumber());
        dto.setEmailAddress(admin.getEmailAddress());
        dto.setExperienceYear(admin.getExperienceYear());
        dto.setAdminLevel(admin.getAdminLevel());
        dto.setCreatedAt(admin.getCreatedAt());

        return dto;
    }
}
