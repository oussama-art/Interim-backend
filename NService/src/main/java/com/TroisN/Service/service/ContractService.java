package com.TroisN.Service.service;

import com.TroisN.Service.dto.contract.ContractCreateRequest;
import com.TroisN.Service.dto.contract.ContractResponse;
import com.TroisN.Service.dto.contract.ContractIntervalCheckResponse;
import com.TroisN.Service.dto.notification.NotificationMessage;
import com.TroisN.Service.entity.Assignment;
import com.TroisN.Service.entity.Candidate;
import com.TroisN.Service.entity.Contract;
import com.TroisN.Service.entity.Demande;
import com.TroisN.Service.enums.CanidateStatus;
import com.TroisN.Service.mapper.ContractMapper;
import com.TroisN.Service.repository.AssignmentRepository;
import com.TroisN.Service.repository.CandidateRepository;
import com.TroisN.Service.repository.ContractRepository;
import com.TroisN.Service.repository.DemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.TroisN.Service.entity.Notification;
import com.TroisN.Service.enums.NotificationRecipientType;
import com.TroisN.Service.event.NotificationCreatedEvent;
import com.TroisN.Service.repository.NotificationRepository;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final CandidateRepository candidateRepository;
    private final DemandeRepository demandeRepository;
    private final ContractMapper contractMapper;
    private final AssignmentRepository assignmentRepository;
    private final NotificationService notificationService;

    @Value("${app.storage.contracts-dir:/app/uploads/contracts}")
    private String contractsDir;

    @Transactional(readOnly = true)
    public Page<ContractResponse> getAllContracts(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "uploadedAt")
        );

        return contractRepository.findAll(pageable)
                .map(contractMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByDemande(Long demandeId)
    {
        demandeRepository.findById(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable avec ID = " + demandeId));
        return contractRepository.findByDemandeId(demandeId)
                .stream()
                .map(contractMapper::toResponse)
                .toList();
    }

    // CREATE

    @Transactional
    public ContractResponse createOrReplaceContract(
            Long candidateId,
            ContractCreateRequest request,
            MultipartFile file
    ) throws IOException {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate introuvable"));

        Demande demande = demandeRepository.findById(request.demandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        validateDates(request.startDate(), request.endDate());

        Path target = storeFile(request.demandeId(), candidateId, file);

        Contract contract = new Contract();
        contract.setCandidate(candidate);
        contract.setDemande(demande);
        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());
        contract.setFilePath(target.toString());
        contract.setOriginalFileName(getOriginalName(file));

        Contract saved = contractRepository.save(contract);

        notifyClient(
                demande,
                candidate,
                "CONTRACT_CREATED",
                "Nouveau contrat disponible",
                "Un nouveau contrat a été ajouté."
        );

        return contractMapper.toResponse(saved);
    }


    @Transactional
    public ContractResponse updateContractFields(
            Long demandeId,
            Long contractId,
            ContractCreateRequest request
    ) {

        if (!demandeId.equals(request.demandeId()))
            throw new IllegalArgumentException("Demande ID incohérent");

        validateDates(request.startDate(), request.endDate());

        Contract contract = contractRepository
                .findByIdAndDemandeId(contractId, demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Contrat introuvable"));

        Long candidateId = contract.getCandidate().getId();

        assertNoContractOverlap(
                candidateId,
                request.startDate(),
                request.endDate(),
                contractId
        );

        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());

        syncAssignmentDates(
                candidateId,
                demandeId,
                request.startDate(),
                request.endDate()
        );

        Contract saved = contractRepository.save(contract);

        notifyClient(
                saved.getDemande(),
                saved.getCandidate(),
                "CONTRACT_DATES_UPDATED",
                "Dates du contrat mises à jour",
                "Les dates du contrat ont été modifiées."
        );

        return contractMapper.toResponse(saved);
    }


    @Transactional
    public ContractResponse updateContractFile(
            Long demandeId,
            Long contractId,
            MultipartFile file
    ) throws IOException {

        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("Fichier contrat requis");

        Contract contract = contractRepository
                .findByIdAndDemandeId(contractId, demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Contrat introuvable"));

        Long candidateId = contract.getCandidate().getId();

        Path target = storeFile(demandeId, candidateId, file);

        deleteFileQuietly(contract.getFilePath());

        contract.setFilePath(target.toString());
        contract.setOriginalFileName(getOriginalName(file));

        Contract saved = contractRepository.save(contract);

        notifyClient(
                saved.getDemande(),
                saved.getCandidate(),
                "CONTRACT_FILE_UPDATED",
                "Fichier du contrat mis à jour",
                "Le fichier du contrat a été remplacé."
        );

        return contractMapper.toResponse(saved);
    }


    // UPDATE: tout

    @Transactional
    public ContractResponse updateContract(
            Long demandeId,
            Long contractId,
            ContractCreateRequest request,
            MultipartFile file
    ) throws IOException {

        if (!demandeId.equals(request.demandeId()))
            throw new IllegalArgumentException("Demande ID incohérent");

        validateDates(request.startDate(), request.endDate());

        Contract contract = contractRepository
                .findByIdAndDemandeId(contractId, demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Contrat introuvable"));

        Long candidateId = contract.getCandidate().getId();

        assertNoContractOverlap(
                candidateId,
                request.startDate(),
                request.endDate(),
                contractId
        );

        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());

        syncAssignmentDates(
                candidateId,
                demandeId,
                request.startDate(),
                request.endDate()
        );

        if (file != null && !file.isEmpty()) {

            Path target = storeFile(demandeId, candidateId, file);

            deleteFileQuietly(contract.getFilePath());

            contract.setFilePath(target.toString());
            contract.setOriginalFileName(getOriginalName(file));
        }

        Contract saved = contractRepository.save(contract);

        notifyClient(
                saved.getDemande(),
                saved.getCandidate(),
                "CONTRACT_UPDATED",
                "Contrat modifié",
                "Le contrat a été mis à jour."
        );

        return contractMapper.toResponse(saved);
    }


    // DELETE

    @Transactional
    public void deleteContract(Long demandeId, Long contractId) {

        Contract contract = contractRepository
                .findByIdAndDemandeId(contractId, demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Contrat introuvable"));

        Demande demande = contract.getDemande();

        deleteFileQuietly(contract.getFilePath());

        contractRepository.delete(contract);

        notifyClient(
                demande,
                contract.getCandidate(),
                "CONTRACT_DELETED",
                "Contrat supprimé",
                "Le contrat associé à votre mission a été supprimé."
        );
    }

    private void notifyClient(
            Demande demande,
            Candidate candidate,
            String type,
            String title,
            String message
    ) {

        String clientUsername = demande.getClient().getEmailAddress();

        String candidateName =
                candidate.getFirstName() + " " + candidate.getLastName();

        String demandeReference = demande.getReference();

        String fullMessage =
                message +
                        " Candidat : " + candidateName +
                        " | Demande : " + demandeReference;

        notificationService.createAndPublish(
                type,
                title,
                fullMessage,
                NotificationRecipientType.USER_QUEUE,
                clientUsername,
                "/client/contracts",
                demande.getId(),
                "CONTRACT"
        );
    }


    // Helpers

    private void validateDates(java.time.LocalDate start, java.time.LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("startDate et endDate sont requis");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("La date de fin doit être >= date de début");
        }
    }

    private String getOriginalName(MultipartFile file) {
        String name = file.getOriginalFilename();
        return (name == null || name.isBlank()) ? "contract.pdf" : name;
    }

    private Path storeFile(Long demandeId, Long candidateId, MultipartFile file) throws IOException {

        Path baseDir = Paths.get(contractsDir, demandeId.toString(), candidateId.toString())
                .toAbsolutePath().normalize();
        Files.createDirectories(baseDir);

        String originalName = getOriginalName(file);
        String safeName = UUID.randomUUID() + "-" + originalName.replaceAll("[\\\\/:*?\"<>|]", "_");

        Path target = baseDir.resolve(safeName).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    private void deleteFileQuietly(String filePath) {
        if (filePath == null || filePath.isBlank()) return;
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (Exception ignored) {}
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadContractFile(Long contractId) throws MalformedURLException {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contrat introuvable avec ID = " + contractId));

        if (contract.getFilePath() == null || contract.getFilePath().isBlank()) {
            throw new IllegalArgumentException("Aucun fichier associé à ce contrat");
        }

        Path filePath = Paths.get(contract.getFilePath()).toAbsolutePath().normalize();

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Fichier introuvable sur le disque");
        }

        Resource resource = new UrlResource(filePath.toUri());

        String contentType = "application/octet-stream";
        try {
            String detected = Files.probeContentType(filePath);
            if (detected != null) contentType = detected;
        } catch (IOException ignored) {}

        String downloadName = (contract.getOriginalFileName() == null || contract.getOriginalFileName().isBlank())
                ? filePath.getFileName().toString()
                : contract.getOriginalFileName();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
                .body(resource);
    }

    private void syncAssignmentDates(Long candidateId, Long demandeId, java.time.LocalDate start, java.time.LocalDate end) {
        Assignment assignment = assignmentRepository
                .findByCandidate_IdAndDemande_Id(candidateId, demandeId)
                .orElseThrow(() -> new IllegalStateException(
                        "Assignment introuvable pour candidateId=" + candidateId + " et demandeId=" + demandeId
                ));

        assignment.setStartDate(start);
        assignment.setEndDate(end);
        assignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public com.TroisN.Service.dto.contract.CandidateAvailabilityResponse getCandidateAvailability(Long candidateId) {

        // Vérifier que candidat existe (optionnel mais propre)
        candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate introuvable avec ID = " + candidateId));

        java.time.LocalDate lastEnd = contractRepository
                .findLastEndDateByCandidateId(candidateId)
                .orElse(null);

        return new com.TroisN.Service.dto.contract.CandidateAvailabilityResponse(candidateId, lastEnd);
    }

    private void assertNoContractOverlap(Long candidateId,
                                         java.time.LocalDate start,
                                         java.time.LocalDate end,
                                         Long excludeContractId) {

        boolean overlap = contractRepository.existsOverlappingContract(
                candidateId, start, end, excludeContractId
        );

        if (overlap) {
            java.time.LocalDate lastEnd = contractRepository
                    .findLastEndDateByCandidateId(candidateId)
                    .orElse(null);

            String msg = "Chevauchement: le candidat a déjà un contrat sur cette période.";
            if (lastEnd != null) {
                msg += " Il sera disponible après le " + lastEnd + ".";
            }
            throw new IllegalStateException(msg);
        }
    }

    @Transactional(readOnly = true)
    public ContractIntervalCheckResponse checkInterval(
            Long candidateId,
            LocalDate start,
            LocalDate end,
            Long excludeContractId
    ) {
        candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate introuvable avec ID = " + candidateId));

        validateDates(start, end);

        // ✅ IMPORTANT : exclure le contrat en cours (quand on édite)
        boolean overlaps = contractRepository.existsOverlappingContract(
                candidateId, start, end, excludeContractId
        );

        LocalDate availableAfter = null;
        String msg;

        if (!overlaps) {
            msg = "OK: aucun chevauchement détecté.";
        } else {
            // (Optionnel) disponible après = max endDate des contrats qui chevauchent vraiment
            // Si tu n’as pas la requête "findOverlappingContracts", garde lastEndDate mais ce sera global.
            LocalDate lastEnd = contractRepository.findLastEndDateByCandidateId(candidateId).orElse(null);
            availableAfter = lastEnd;

            msg = "Chevauchement détecté avec un autre contrat.";
            if (availableAfter != null) {
                msg += " Disponible après le " + availableAfter + ".";
            }
        }

        return new ContractIntervalCheckResponse(
                candidateId, start, end, overlaps, availableAfter, msg
        );
    }
}