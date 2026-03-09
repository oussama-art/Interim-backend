package com.TroisN.Service.dto.candidateNoAuth;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record CandidateUploadCvRequest(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank
        String professional,

        MultipartFile cv

) {}