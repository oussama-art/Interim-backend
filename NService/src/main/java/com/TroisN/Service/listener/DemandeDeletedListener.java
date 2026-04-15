package com.TroisN.Service.listener;

import com.TroisN.Service.entity.Assignment;
import com.TroisN.Service.entity.Candidate;
import com.TroisN.Service.enums.CanidateStatus;
import com.TroisN.Service.event.DemandeDeletedEvent;
import com.TroisN.Service.repository.AssignmentRepository;
import com.TroisN.Service.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemandeDeletedListener {

    private final AssignmentRepository assignmentRepository;
    private final CandidateRepository candidateRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DemandeDeletedEvent event) {

        log.info("=== DemandeDeletedListener triggered ===");
        log.info("Demande ID: {}", event.demandeId());

        List<Assignment> assignments = event.assignments();

        log.info("Assignments received in event: {}", assignments.size());

        // éviter de traiter plusieurs fois le même candidat
        Set<Long> processedCandidates = new HashSet<>();

        for (Assignment assignment : assignments) {

            Candidate candidate = assignment.getCandidate();

            if (candidate == null) {
                log.warn("Assignment {} has no candidate", assignment.getId());
                continue;
            }

            Long candidateId = candidate.getId();

            // éviter duplication
            if (!processedCandidates.add(candidateId)) {
                continue;
            }

            log.info("Processing candidate ID: {}", candidateId);
            log.info("Candidate current status: {}", candidate.getStatus());

            boolean hasOtherAssignments =
                    assignmentRepository.existsByCandidate_Id(candidateId);

            log.info("Candidate {} has other assignments: {}", candidateId, hasOtherAssignments);

            if (!hasOtherAssignments) {

                log.info("Updating candidate {} status to AVAILABLE", candidateId);

                candidateRepository.updateStatus(candidateId, CanidateStatus.AVAILABLE);

                log.info("Candidate {} status updated", candidateId);

            } else {

                log.info("Candidate {} still has assignments, status not changed", candidateId);

            }
        }

        log.info("=== End DemandeDeletedListener ===");
    }
}