package com.TroisN.Service.controller;

import com.TroisN.Service.dto.CreatedAccountInfo;
import com.TroisN.Service.dto.account.AccountApprovalRequest;
import com.TroisN.Service.dto.account.AccountCreationRequest;
import com.TroisN.Service.dto.account.AccountCreationResponse;
import com.TroisN.Service.dto.account.EmailCheckResponse;
import com.TroisN.Service.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AccountController {

    private final AccountService accountService;


    @PostMapping("/create")
    @PreAuthorize("permitAll()")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountCreationResponse create(
            @Valid @RequestBody AccountCreationRequest dto) {
        return accountService.create(dto);
    }

    @GetMapping("/check-email")
    @PreAuthorize("permitAll()")
    public EmailCheckResponse checkEmail(
            @RequestParam String email) {

        return accountService.checkEmailExists(email);
    }

    @GetMapping
    public List<AccountCreationResponse> getAll() {
        return accountService.getAll();
    }


    @GetMapping("/{id}")
    public AccountCreationResponse getById(@PathVariable Long id) {
        return accountService.getById(id);
    }


    @PutMapping("/{id}")
    public AccountCreationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AccountCreationRequest dto) {
        return accountService.update(id, dto);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        accountService.delete(id);
    }


    @PatchMapping("/{id}/approve")
    public List<CreatedAccountInfo> approve(
            @PathVariable Long id,
            @RequestBody AccountApprovalRequest request
    ) {
        return accountService.approve(id, request.getSelectedEmails());
    }


    @PatchMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(
            @PathVariable Long id,
            @RequestParam String reason) {
        accountService.reject(id, reason);
    }
}
