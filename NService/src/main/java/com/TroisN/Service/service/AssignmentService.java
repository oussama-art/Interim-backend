package com.TroisN.Service.service;

import com.TroisN.Service.dto.assignment.AssignmentCreateRequest;
import com.TroisN.Service.dto.assignment.AssignmentResponse;
import com.TroisN.Service.entity.*;
import com.TroisN.Service.enums.CanidateStatus;
import com.TroisN.Service.mapper.AssignmentMapper;
import com.TroisN.Service.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Service
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CandidateRepository candidateRepository;
    private final DemandeRepository demandeRepository;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            CandidateRepository candidateRepository,
            DemandeRepository demandeRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.candidateRepository = candidateRepository;
        this.demandeRepository = demandeRepository;
    }


    @Transactional
    public AssignmentResponse acceptCandidate(AssignmentCreateRequest request) {

        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("Candidat introuvable"));

        if (candidate.getStatus() != CanidateStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Le candidat n'est pas disponible (statut = " + candidate.getStatus() + ")"
            );
        }

        Demande demande = demandeRepository.findById(request.getDemandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        Assignment assignment = new Assignment();
        assignment.setCandidate(candidate);
        assignment.setDemande(demande);
        assignment.setClient(demande.getClient());
        assignment.setStartDate(request.getStartDate());
        assignment.setEndDate(request.getEndDate());

        Assignment saved = assignmentRepository.save(assignment);

        candidate.setStatus(CanidateStatus.ON_MISSION);

        return AssignmentMapper.toResponse(saved);
    }

    public Page<AssignmentResponse> getAssignments(Pageable pageable) {

        Page<Assignment> page = assignmentRepository.findAll(pageable);

        return page.map(AssignmentMapper::toResponse);
    }


}
