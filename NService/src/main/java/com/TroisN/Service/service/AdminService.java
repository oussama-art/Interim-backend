package com.TroisN.Service.service;

import com.TroisN.Service.dto.admin.AdminCreateRequest;
import com.TroisN.Service.dto.admin.AdminPatchRequest;
import com.TroisN.Service.dto.admin.AdminResponse;
import com.TroisN.Service.entity.Admin;
import com.TroisN.Service.entity.Demande;
import com.TroisN.Service.mapper.AdminMapper;
import com.TroisN.Service.repository.AdminRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final KeycloakUserService keycloakUserService;
    public EmailService emailService;

    public AdminService(AdminRepository adminRepository, KeycloakUserService keycloakUserService,
                        EmailService emailService) {
        this.adminRepository = adminRepository;
        this.keycloakUserService = keycloakUserService;
        this.emailService = emailService;
    }

    public Page<AdminResponse> getAllAdmins(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return adminRepository.findAll(pageable).map(AdminMapper::toResponseDTO);
    }

    public AdminResponse createAdmin(AdminCreateRequest dto) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        try {
            String keycloakUserId = keycloakUserService.createAdminUser(
                    dto.getEmailAddress(),
                    dto.getPassword(),
                    dto.getFirstName(),
                    dto.getLastName()
            );

            Admin admin = new Admin();
            admin.setFirstName(dto.getFirstName());
            admin.setLastName(dto.getLastName());
            admin.setPhoneNumber(dto.getPhoneNumber());
            admin.setEmailAddress(dto.getEmailAddress());
            admin.setExperienceYear(dto.getExperienceYear());
            admin.setAdminLevel(dto.getAdminLevel());
            admin.setKeycloakUserId(keycloakUserId);

            Admin saved = adminRepository.save(admin);
            return AdminMapper.toResponseDTO(saved);

        } catch (IllegalStateException ex) {

            String message = ex.getMessage() != null ? ex.getMessage() : "";

            if (message.contains("User exists with same email")) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A user with this email already exists"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while creating admin account"
            );
        }
    }

    public AdminResponse getAdminDetails(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with id " + id));
        return AdminMapper.toResponseDTO(admin);
    }

    public void deleteAdmin(Long id) {
        if (!adminRepository.existsById(id)) {
            throw new EntityNotFoundException("Admin not found with id " + id);
        }
        adminRepository.deleteById(id);
    }

    public AdminResponse patchAdmin(AdminPatchRequest dto, Long id) {

        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with id " + id));

        if (dto.getFirstName() != null)
            admin.setFirstName(dto.getFirstName());

        if (dto.getLastName() != null)
            admin.setLastName(dto.getLastName());

        if (dto.getEmailAddress() != null)
            admin.setEmailAddress(dto.getEmailAddress());

        if (dto.getPhoneNumber() != null)
            admin.setPhoneNumber(dto.getPhoneNumber());

        if (dto.getExperienceYear() != null)
            admin.setExperienceYear(dto.getExperienceYear());

        if (dto.getAdminLevel() != null)
            admin.setAdminLevel(dto.getAdminLevel());

        Admin updated = adminRepository.save(admin);
        return AdminMapper.toResponseDTO(updated);
    }

    public void notifyAdmins(Demande demande) {

        List<String> adminEmails = adminRepository.findAllAdminEmails();

        if (adminEmails.isEmpty()) {
            return;
        }

        String subject = "New Demand Created";

        String content = """
        A new demand has been created.

        Client: %s
        Demand ID: %d
        Created at: %s

        Please log in to the admin dashboard to review it.
        """.formatted(
                demande.getClient().getTitle(),
                demande.getId(),
                demande.getCreatedAt()
        );

        emailService.sendEmail(adminEmails, subject, content);
    }

}
