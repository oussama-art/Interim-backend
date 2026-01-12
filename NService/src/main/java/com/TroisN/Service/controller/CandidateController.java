package com.TroisN.Service.controller;

import com.TroisN.Service.dto.candidate.*;
import com.TroisN.Service.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }



    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CandidateResponse createCandidate(
            @Valid @RequestBody CandidateCreateRequest dto
    ) {
        return candidateService.createCandidate(dto);
    }



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public Page<CandidateResponse> getAllCandidates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return candidateService.getAllCandidates(page, size);
    }



    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/me")
    public CandidateResponse getMe(Authentication authentication) {
        return candidateService.getCandidateFromKeycloak(authentication);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @PatchMapping("/me")
    public CandidateResponse patchMe(
            Authentication authentication,
            @Valid @RequestBody CandidatePatchRequest dto
    ) {
        return candidateService.patchCandidate(authentication, dto);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @PatchMapping(value = "/me/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CandidateResponse updateCv(
            Authentication authentication,
            @RequestPart("cv") MultipartFile cvFile
    ) throws IOException {
        return candidateService.updateCandidateCv(authentication, cvFile);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @DeleteMapping("/me")
    public void deleteMe(Authentication authentication) {
        candidateService.deleteCandidate(authentication);
    }


    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> getCandidateById(@PathVariable Long id) {
        CandidateResponse response = candidateService.getCandidateById(id);
        return ResponseEntity.ok(response);
    }
}
