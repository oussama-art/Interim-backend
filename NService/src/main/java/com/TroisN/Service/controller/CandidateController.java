package com.TroisN.Service.controller;

import com.TroisN.Service.dto.candidate.*;
import com.TroisN.Service.dto.candidateNoAuth.CandidateNoAuthResponse;
import com.TroisN.Service.dto.candidateNoAuth.CandidateUploadCvRequest;
import com.TroisN.Service.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CandidateResponse> createCandidate(
            @Valid @RequestBody CandidateCreateRequest dto
    ) {
        return ResponseEntity.ok(candidateService.createCandidate(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/available")
    public ResponseEntity<List<CandidateResponse>> getAvailableCandidates(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return ResponseEntity.ok(
                candidateService.getAvailableCandidatesForPeriod(startDate, endDate)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public ResponseEntity<Page<CandidateResponse>> getAllCandidates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(candidateService.getAllCandidates(page, size));
    }

    /*
    ===== Endpoints Keycloak désactivés temporairement =====

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
    */

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<CandidateResponse> patchCandidate(
            @PathVariable Long id,
            @Valid @RequestBody CandidatePatchRequest dto
    ) {
        return ResponseEntity.ok(candidateService.patchCandidateById(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(
            value = "/{id}/cv",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CandidateResponse> updateCandidateCv(
            @PathVariable Long id,
            @RequestPart("cv") MultipartFile cvFile
    ) throws IOException {
        return ResponseEntity.ok(candidateService.updateCandidateCvById(id, cvFile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> getCandidateById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidateById(@PathVariable Long id) {
        candidateService.deleteCandidateById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/cv")
    public ResponseEntity<Resource> getCandidateCv(@PathVariable Long id) throws IOException {
        Resource resource = candidateService.getCandidateCvResource(id);

        String contentType = URLConnection.guessContentTypeFromName(resource.getFilename());
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\""
                )
                .body(resource);
    }

    @PostMapping(
            value = "/upload-cv",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CandidateResponse> uploadCandidate(
            @ModelAttribute CandidateUploadCvRequest request
    ) throws IOException {

        return ResponseEntity.ok(
                candidateService.createCandidateFromCv(request)
        );
    }
}