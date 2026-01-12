package com.TroisN.Service.service;

import com.TroisN.Service.dto.candidate.*;
import com.TroisN.Service.entity.Candidate;
import com.TroisN.Service.mapper.CandidateMapper;
import com.TroisN.Service.repository.CandidateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final KeycloakUserService keycloakUserService;

    public CandidateService(
            CandidateRepository candidateRepository,
            KeycloakUserService keycloakUserService
    ) {
        this.candidateRepository = candidateRepository;
        this.keycloakUserService = keycloakUserService;
    }



    public Page<CandidateResponse> getAllCandidates(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return candidateRepository.findAll(pageable)
                .map(CandidateMapper::toResponseDTO);
    }



    @Transactional
    public CandidateResponse createCandidate(CandidateCreateRequest dto) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }

        String keycloakUserId = null;

        try {
            keycloakUserId = keycloakUserService.createCandidateUser(dto);

            Candidate candidate = new Candidate();
            candidate.setFirstName(dto.getFirstName());
            candidate.setLastName(dto.getLastName());
            candidate.setPhoneNumber(dto.getPhoneNumber());
            candidate.setEmailAddress(dto.getEmailAddress());
            candidate.setExperienceYear(dto.getExperienceYear());
            candidate.setSkills(dto.getSkills());
            candidate.setProfessional(dto.getProfessional());
            candidate.setCin(dto.getCin());
            candidate.setCssNumber(dto.getCssNumber());
            candidate.setKeycloakUserId(keycloakUserId);

            Candidate saved = candidateRepository.save(candidate);
            return CandidateMapper.toResponseDTO(saved);

        } catch (Exception ex) {
            if (keycloakUserId != null) {
                keycloakUserService.deleteUser(keycloakUserId);
            }
            throw ex;
        }
    }


    public CandidateResponse getCandidateFromKeycloak(Authentication authentication) {
        Candidate candidate = getCandidateEntity(authentication);
        return CandidateMapper.toResponseDTO(candidate);
    }

    @Transactional
    public CandidateResponse patchCandidate(Authentication authentication, CandidatePatchRequest dto) {

        Candidate candidate = getCandidateEntity(authentication);

        if (dto.getFirstName() != null) candidate.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) candidate.setLastName(dto.getLastName());
        if (dto.getEmailAddress() != null) candidate.setEmailAddress(dto.getEmailAddress());
        if (dto.getPhoneNumber() != null) candidate.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getExperienceYear() != null) candidate.setExperienceYear(dto.getExperienceYear());
        if (dto.getSkills() != null) candidate.setSkills(dto.getSkills());
        if (dto.getProfessional() != null) candidate.setProfessional(dto.getProfessional());
        if (dto.getCin() != null) candidate.setCin(dto.getCin());
        if (dto.getCssNumber() != null) candidate.setCssNumber(dto.getCssNumber());

        return CandidateMapper.toResponseDTO(candidateRepository.save(candidate));
    }

    @Transactional
    public CandidateResponse updateCandidateCv(Authentication authentication, MultipartFile cvFile)
            throws IOException {

        Candidate candidate = getCandidateEntity(authentication);

        if (cvFile == null || cvFile.isEmpty()) {
            throw new IllegalArgumentException("Fichier PDF obligatoire");
        }

        if (!"application/pdf".equalsIgnoreCase(cvFile.getContentType())) {
            throw new IllegalArgumentException("Seuls les fichiers PDF sont autorisés");
        }

        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "cv");
        Files.createDirectories(uploadPath);

        if (candidate.getCvPath() != null) {
            Files.deleteIfExists(Paths.get(candidate.getCvPath()));
        }

        String fileName = UUID.randomUUID() + "_" + cvFile.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, cvFile.getBytes());

        candidate.setCvPath(filePath.toString());
        return CandidateMapper.toResponseDTO(candidateRepository.save(candidate));
    }

    @Transactional
    public void deleteCandidate(Authentication authentication) {
        Candidate candidate = getCandidateEntity(authentication);
        candidateRepository.delete(candidate);
    }


    private Candidate getCandidateEntity(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();

        return candidateRepository.findByKeycloakUserId(keycloakId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Candidat introuvable pour l'utilisateur connecté")
                );
    }


    public CandidateResponse getCandidateById(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Candidat introuvable avec l'id : " + id)
                );

        return CandidateMapper.toResponseDTO(candidate);
    }

}
