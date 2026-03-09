package com.TroisN.Service.controller;

import com.TroisN.Service.dto.admin.AdminCreateRequest;
import com.TroisN.Service.dto.admin.AdminPatchRequest;
import com.TroisN.Service.dto.admin.AdminResponse;
import com.TroisN.Service.dto.admin.SuspendUserRequest;
import com.TroisN.Service.service.AdminService;
import com.TroisN.Service.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/admins")

public class AdminController {

    private final AdminService adminService;
    private final LoginService loginService;

    public AdminController(AdminService adminService, LoginService loginService){
        this.adminService = adminService;
        this.loginService = loginService;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<AdminResponse> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return adminService.getAllAdmins(page, size);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public AdminResponse getAdminDetails(@PathVariable Long id){
        return adminService.getAdminDetails(id);
    }

    @PostMapping("/create")
    public AdminResponse createAdmin(
            @Valid @RequestBody AdminCreateRequest dto
    ) throws IOException {
        return adminService.createAdmin(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteAdmin(@PathVariable Long id){
        adminService.deleteAdmin(id);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public AdminResponse patchAdmin(
            @PathVariable Long id,
            @Valid @ModelAttribute AdminPatchRequest dto
    ){
        return adminService.patchAdmin(dto,id);
    }

    @PatchMapping("/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> suspendUser(
            @RequestBody SuspendUserRequest request
    ) {

        if (request.getSuspendMinutes() == null) {
            loginService.suspendUser(request.getEmail());
        } else {
            loginService.suspendUserTemporarily(
                    request.getEmail(),
                    request.getSuspendMinutes()
            );
        }

        return ResponseEntity.ok().build();
    }

}
