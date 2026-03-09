package com.TroisN.Service.controller;

import com.TroisN.Service.dto.contract.ContractCreateRequest;
import com.TroisN.Service.dto.contract.ContractIntervalCheckResponse;
import com.TroisN.Service.dto.contract.ContractResponse;
import com.TroisN.Service.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import com.TroisN.Service.dto.contract.CandidateAvailabilityResponse;


import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contracts")

public class ContractController {

    private final ContractService contractService;

    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    @GetMapping("/{demandeId}/contracts")
    public List<ContractResponse> getContracts(@PathVariable Long demandeId) {
        return contractService.getContractsByDemande(demandeId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<ContractResponse> getAllContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return contractService.getAllContracts(page, size);
    }


    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    @GetMapping("/{contractId}/download")
    public ResponseEntity<Resource> downloadContract(@PathVariable Long contractId) throws Exception {
        return contractService.downloadContractFile(contractId);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            value = "/{demandeId}/candidates/{candidateId}/contract",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ContractResponse uploadContract(
            @PathVariable Long demandeId,
            @PathVariable Long candidateId,
            @Valid @RequestPart("metadata") ContractCreateRequest metadata,
            @RequestPart("file") MultipartFile file
    ) throws IOException {

        if (!demandeId.equals(metadata.demandeId())) {
            throw new IllegalArgumentException("Demande ID incohérent (URL vs metadata)");
        }

        return contractService.createOrReplaceContract(candidateId, metadata, file);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(
            value = "/{demandeId}/contracts/{contractId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ContractResponse updateContract(
            @PathVariable Long demandeId,
            @PathVariable Long contractId,
            @Valid @RequestPart("metadata") ContractCreateRequest metadata,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        return contractService.updateContract(demandeId, contractId, metadata, file);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(
            value = "/{demandeId}/contracts/{contractId}/fields",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ContractResponse updateFields(
            @PathVariable Long demandeId,
            @PathVariable Long contractId,
            @Valid @RequestBody ContractCreateRequest body
    ) {
        return contractService.updateContractFields(demandeId, contractId, body);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(
            value = "/{demandeId}/contracts/{contractId}/file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ContractResponse updateFile(
            @PathVariable Long demandeId,
            @PathVariable Long contractId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return contractService.updateContractFile(demandeId, contractId, file);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{demandeId}/contracts/{contractId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContract(
            @PathVariable Long demandeId,
            @PathVariable Long contractId
    ) {
        contractService.deleteContract(demandeId, contractId);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/candidates/{candidateId}/availability")
    public CandidateAvailabilityResponse getAvailability(@PathVariable Long candidateId) {
        return contractService.getCandidateAvailability(candidateId);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/candidates/{candidateId}/check-interval")
    public ContractIntervalCheckResponse checkInterval(
            @PathVariable Long candidateId,
            @RequestParam("start") LocalDate start,
            @RequestParam("end") LocalDate end,
            @RequestParam(value = "excludeContractId", required = false) Long excludeContractId
    ) {
        return contractService.checkInterval(candidateId, start, end, excludeContractId);
    }

}