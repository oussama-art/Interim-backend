package com.TroisN.Service.service;

import com.TroisN.Service.dto.admin.AdminCreateRequest;
import com.TroisN.Service.dto.admin.AdminPatchRequest;
import com.TroisN.Service.dto.admin.AdminResponse;
import com.TroisN.Service.entity.Admin;
import com.TroisN.Service.entity.ClientCompany;
import com.TroisN.Service.entity.Demande;
import com.TroisN.Service.repository.AdminRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    // =========================
    // Mocks (dependencies)
    // =========================
    @Mock
    private AdminRepository adminRepository;

    @Mock
    private KeycloakUserService keycloakUserService;

    @Mock
    private EmailService emailService;

    // =========================
    // Class under test
    // =========================
    @InjectMocks
    private AdminService adminService;

    // =========================
    // createAdmin()
    // =========================

    @Test
    void shouldCreateAdminSuccessfully() {

        // Arrange
        AdminCreateRequest dto = new AdminCreateRequest();
        dto.setEmailAddress("admin@test.com");
        dto.setPassword("1234");
        dto.setConfirmPassword("1234");
        dto.setFirstName("John");
        dto.setLastName("Doe");

        when(keycloakUserService.createAdminUser(
                anyString(), anyString(), anyString(), anyString()
        )).thenReturn("kc-123");

        when(adminRepository.save(any(Admin.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AdminResponse response = adminService.createAdmin(dto);

        // Assert
        assertNotNull(response);
        assertEquals("admin@test.com", response.getEmailAddress());

        verify(keycloakUserService).createAdminUser(
                dto.getEmailAddress(),
                dto.getPassword(),
                dto.getFirstName(),
                dto.getLastName()
        );
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void shouldThrowBadRequestWhenPasswordsDoNotMatch() {

        AdminCreateRequest dto = new AdminCreateRequest();
        dto.setPassword("1234");
        dto.setConfirmPassword("5678");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> adminService.createAdmin(dto)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());

        verifyNoInteractions(keycloakUserService, adminRepository);
    }

    @Test
    void shouldThrowConflictWhenEmailAlreadyExistsInKeycloak() {

        AdminCreateRequest dto = new AdminCreateRequest();
        dto.setEmailAddress("admin@test.com");
        dto.setPassword("1234");
        dto.setConfirmPassword("1234");

        when(keycloakUserService.createAdminUser(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("User exists with same email"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> adminService.createAdmin(dto)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // =========================
    // getAdminDetails()
    // =========================

    @Test
    void shouldReturnAdminDetailsWhenFound() {

        Admin admin = new Admin();
        admin.setId(1L);
        admin.setEmailAddress("admin@test.com");

        when(adminRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        AdminResponse response = adminService.getAdminDetails(1L);

        assertNotNull(response);
        assertEquals("admin@test.com", response.getEmailAddress());
    }

    @Test
    void shouldThrowExceptionWhenAdminNotFound() {

        when(adminRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> adminService.getAdminDetails(99L)
        );
    }

    // =========================
    // deleteAdmin()
    // =========================

    @Test
    void shouldDeleteAdminWhenExists() {

        when(adminRepository.existsById(1L))
                .thenReturn(true);

        adminService.deleteAdmin(1L);

        verify(adminRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingAdmin() {

        when(adminRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                EntityNotFoundException.class,
                () -> adminService.deleteAdmin(99L)
        );
    }

    // =========================
    // patchAdmin()
    // =========================

    @Test
    void shouldPatchAdminSuccessfully() {

        Admin admin = new Admin();
        admin.setId(1L);
        admin.setFirstName("Old");

        AdminPatchRequest dto = new AdminPatchRequest();
        dto.setFirstName("New");

        when(adminRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        when(adminRepository.save(any(Admin.class)))
                .thenReturn(admin);

        AdminResponse response = adminService.patchAdmin(dto, 1L);

        assertNotNull(response);
        assertEquals("New", response.getFirstName());
    }

    // =========================
    // notifyAdmins()
    // =========================

    @Test
    void shouldSendEmailToAdminsWhenAdminsExist() {

        // Arrange
        Demande demande = new Demande();
        demande.setId(10L);
        demande.setCreatedAt(LocalDateTime.now());

        ClientCompany client = new ClientCompany();
        client.setTitle("ACME Corp");

        demande.setClient(client);

        when(adminRepository.findAllAdminEmails())
                .thenReturn(List.of("a@test.com", "b@test.com"));

        // Act
        adminService.notifyAdmins(demande);

        // Assert
        verify(emailService).sendEmail(
                anyList(),
                anyString(),
                anyString()
        );
    }


    @Test
    void shouldNotSendEmailWhenNoAdmins() {

        when(adminRepository.findAllAdminEmails())
                .thenReturn(List.of());

        adminService.notifyAdmins(new Demande());

        verifyNoInteractions(emailService);
    }

    @Test
    void shouldReturnPagedAdmins() {

        // Arrange
        Admin admin1 = new Admin();
        admin1.setId(1L);
        admin1.setEmailAddress("a@test.com");

        Admin admin2 = new Admin();
        admin2.setId(2L);
        admin2.setEmailAddress("b@test.com");

        Page<Admin> page = new org.springframework.data.domain.PageImpl<>(
                List.of(admin1, admin2)
        );

        when(adminRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        // Act
        Page<AdminResponse> result = adminService.getAllAdmins(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("a@test.com", result.getContent().get(0).getEmailAddress());

        verify(adminRepository).findAll(any(Pageable.class));
    }

    @Test
    void shouldThrowExceptionWhenPatchingNonExistingAdmin() {

        AdminPatchRequest dto = new AdminPatchRequest();
        dto.setFirstName("New");

        when(adminRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> adminService.patchAdmin(dto, 99L)
        );

        verify(adminRepository, never()).save(any());
    }

    @Test
    void shouldThrowInternalServerErrorForUnexpectedKeycloakError() {

        AdminCreateRequest dto = new AdminCreateRequest();
        dto.setEmailAddress("admin@test.com");
        dto.setPassword("1234");
        dto.setConfirmPassword("1234");
        dto.setFirstName("John");
        dto.setLastName("Doe");

        when(keycloakUserService.createAdminUser(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("Unexpected error"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> adminService.createAdmin(dto)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    }



}
