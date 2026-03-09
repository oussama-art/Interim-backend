package com.TroisN.Service.service;

import com.TroisN.Service.dto.candidate.CandidateCreateRequest;
import com.TroisN.Service.dto.candidate.CandidatePatchRequest;
import com.TroisN.Service.dto.candidate.CandidateResponse;
import com.TroisN.Service.dto.candidateNoAuth.CandidateNoAuthResponse;
import com.TroisN.Service.dto.candidateNoAuth.CandidateUploadCvRequest;
import com.TroisN.Service.entity.Candidate;
import com.TroisN.Service.mapper.CandidateMapper;
import com.TroisN.Service.repository.CandidateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    public Page<CandidateResponse> getAllCandidates(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return candidateRepository.findAll(pageable)
                .map(CandidateMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<CandidateResponse> getAvailableCandidatesForPeriod(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "La date de fin doit être postérieure ou égale à la date de début"
            );
        }

        return candidateRepository.findAvailableCandidatesInPeriod(startDate, endDate)
                .stream()
                .map(CandidateMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public CandidateResponse createCandidate(CandidateCreateRequest dto) {

        if (dto.getEmailAddress() != null
                && !dto.getEmailAddress().isBlank()
                && candidateRepository.existsByEmailAddress(dto.getEmailAddress())) {
            throw new IllegalArgumentException("Un candidat avec cet email existe déjà");
        }

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

        Candidate saved = candidateRepository.save(candidate);
        return CandidateMapper.toResponseDTO(saved);

        /*
        ===== Ancienne version Keycloak à réactiver plus tard =====

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
        */
    }

    /*
    ===== Désactivé temporairement car plus de Keycloak =====

    public CandidateResponse getCandidateFromKeycloak(Authentication authentication) {
        Candidate candidate = getCandidateEntity(authentication);
        return CandidateMapper.toResponseDTO(candidate);
    }

    @Transactional
    public CandidateResponse patchCandidate(Authentication authentication, CandidatePatchRequest dto) {
        Candidate candidate = getCandidateEntity(authentication);
        ...
    }

    @Transactional
    public CandidateResponse updateCandidateCv(Authentication authentication, MultipartFile cvFile)
            throws IOException {
        Candidate candidate = getCandidateEntity(authentication);
        ...
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
    */

    public CandidateResponse getCandidateById(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Candidat introuvable avec l'id : " + id)
                );

        return CandidateMapper.toResponseDTO(candidate);
    }

    @Transactional
    public CandidateResponse patchCandidateById(Long candidateId, CandidatePatchRequest dto) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Candidat introuvable avec l'id : " + candidateId)
                );

        if (dto.getFirstName() != null) candidate.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) candidate.setLastName(dto.getLastName());

        if (dto.getEmailAddress() != null) {
            if (!dto.getEmailAddress().isBlank()
                    && candidateRepository.existsByEmailAddress(dto.getEmailAddress())
                    && (candidate.getEmailAddress() == null
                    || !candidate.getEmailAddress().equals(dto.getEmailAddress()))) {
                throw new IllegalArgumentException("Un candidat avec cet email existe déjà");
            }
            candidate.setEmailAddress(dto.getEmailAddress());
        }

        if (dto.getPhoneNumber() != null) candidate.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getExperienceYear() != null) candidate.setExperienceYear(dto.getExperienceYear());
        if (dto.getSkills() != null) candidate.setSkills(dto.getSkills());
        if (dto.getProfessional() != null) candidate.setProfessional(dto.getProfessional());
        if (dto.getCin() != null) candidate.setCin(dto.getCin());
        if (dto.getCssNumber() != null) candidate.setCssNumber(dto.getCssNumber());

        return CandidateMapper.toResponseDTO(candidateRepository.save(candidate));
    }

    @Transactional
    public CandidateResponse updateCandidateCvById(Long candidateId, MultipartFile cvFile)
            throws IOException {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Candidat introuvable avec l'id : " + candidateId)
                );

        if (cvFile == null || cvFile.isEmpty()) {
            throw new IllegalArgumentException("Le fichier CV est obligatoire");
        }

        Set<String> allowedContentTypes = Set.of(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "image/jpeg",
                "image/png"
        );

        String contentType = cvFile.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Type de fichier non autorisé. Formats acceptés : PDF, Word (DOC/DOCX), JPG, PNG"
            );
        }

        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "cv");
        Files.createDirectories(uploadPath);

        if (candidate.getCvPath() != null && !candidate.getCvPath().isBlank()) {
            Files.deleteIfExists(Paths.get(candidate.getCvPath()));
        }

        String originalFileName = cvFile.getOriginalFilename();
        String fileExtension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID() + fileExtension;
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(cvFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        candidate.setCvPath(filePath.toString());
        candidateRepository.save(candidate);

        return CandidateMapper.toResponseDTO(candidate);
    }

    public Resource getCandidateCvResource(Long candidateId) throws IOException {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Candidat introuvable")
                );

        if (candidate.getCvPath() == null || candidate.getCvPath().isBlank()) {
            throw new EntityNotFoundException("Aucun CV disponible");
        }

        return loadCv(candidate);

        /*
        ===== Ancienne version avec sécurité Keycloak =====

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakUserId = jwt.getSubject();

        if (hasRole(jwt, "ADMIN")) {
            return loadCv(candidate);
        }

        if (hasRole(jwt, "CANDIDATE")
                && candidate.getKeycloakUserId().equals(keycloakUserId)) {
            return loadCv(candidate);
        }

        if (hasRole(jwt, "CLIENT")) {
            ClientCompany client = clientRepository
                    .findBykeycloakUserId(keycloakUserId)
                    .orElseThrow(() ->
                            new EntityNotFoundException("Client introuvable")
                    );

            boolean hasAccess = candidateRepository
                    .existsCandidateAppliedToClientOffers(candidate.getId(), client.getId());

            if (hasAccess) {
                return loadCv(candidate);
            }
        }

        throw new SecurityException("Accès interdit au CV");
        */
    }

    private Resource loadCv(Candidate candidate) throws IOException {
        Path path = Paths.get(candidate.getCvPath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new EntityNotFoundException("Fichier CV introuvable");
        }

        return resource;
    }

    /*
    ===== Désactivé temporairement car plus de Jwt =====

    private boolean hasRole(Jwt jwt, String role) {
        var realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return false;

        var roles = (java.util.Map<?, ?>) realmAccess;
        var list = (java.util.List<?>) roles.get("roles");

        return list.contains(role);
    }
    */

    @Transactional
    public void deleteCandidateById(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Candidat introuvable avec l'id : " + candidateId)
                );

        if (candidate.getCvPath() != null && !candidate.getCvPath().isBlank()) {
            try {
                Files.deleteIfExists(Paths.get(candidate.getCvPath()));
            } catch (IOException e) {
                System.err.println("Erreur lors de la suppression du CV: " + e.getMessage());
            }
        }

        candidateRepository.delete(candidate);

        /*
        ===== Ancienne suppression Keycloak =====

        if (candidate.getKeycloakUserId() != null) {
            try {
                keycloakUserService.deleteUser(candidate.getKeycloakUserId());
            } catch (Exception e) {
                System.err.println("Erreur lors de la suppression de l'utilisateur Keycloak: " + e.getMessage());
            }
        }

        candidateRepository.delete(candidate);
        */
    }
    @Transactional
    public CandidateResponse createCandidateFromCv(CandidateUploadCvRequest request) throws IOException {

        MultipartFile cvFile = request.cv();

        if (cvFile == null || cvFile.isEmpty()) {
            throw new IllegalArgumentException("Le fichier CV est obligatoire");
        }

        if (request.firstName() == null || request.firstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom est obligatoire");
        }

        if (request.lastName() == null || request.lastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }

        String firstName = request.firstName().trim();
        String lastName = request.lastName().trim();

        boolean candidateExists = candidateRepository
                .existsByFirstNameAndLastName(firstName, lastName);

        if (candidateExists) {
            throw new IllegalArgumentException("Ce candidat existe déjà");
        }

        Set<String> allowedTypes = Set.of(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "image/jpeg",
                "image/jpg",
                "image/png"
        );

        String contentType = cvFile.getContentType();

        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Format non autorisé. Formats acceptés : PDF, DOC, DOCX, JPG, PNG"
            );
        }

        String originalFileName = cvFile.getOriginalFilename();
        String fileExtension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + fileExtension;

        Path uploadDir = Paths.get("uploads", "cv");

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path filePath = uploadDir.resolve(fileName);

        Files.copy(cvFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Candidate candidate = new Candidate();
        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);
        candidate.setProfessional(request.professional());
        candidate.setCvPath(filePath.toString());
        candidate.setStatus(com.TroisN.Service.enums.CanidateStatus.AVAILABLE);
        candidate.setNextAvailableDate(LocalDate.now());

        Candidate savedCandidate = candidateRepository.save(candidate);

        return CandidateMapper.toResponseDTO(savedCandidate);
    }
}