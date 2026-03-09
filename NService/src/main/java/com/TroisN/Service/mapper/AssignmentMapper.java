package com.TroisN.Service.mapper;

import com.TroisN.Service.dto.assignment.AssignmentResponse;
import com.TroisN.Service.entity.Assignment;

public class AssignmentMapper {

    private AssignmentMapper() {}

    public static AssignmentResponse toResponse(Assignment assignment) {

        AssignmentResponse dto = new AssignmentResponse();

//        dto.setAssignmentId(assignment.getId());
//
//        // Candidate
//        dto.setCandidateId(assignment.getCandidate().getId());
        dto.setCandidateName(
                assignment.getCandidate().getFirstName() + " " +
                        assignment.getCandidate().getLastName()
        );
        dto.setCandidateStatus(assignment.getCandidate().getStatus().name());

        // Demande
//        dto.setDemandeId(assignment.getDemande().getId());
        dto.setDemandeTitle(assignment.getDemande().getTitle());

        // Client
//        dto.setClientId(assignment.getClient().getId());
        dto.setClientName(assignment.getClient().getTitle());

        // Dates
        dto.setStartDate(assignment.getStartDate());
        dto.setEndDate(assignment.getEndDate());

        dto.setDemandeId(assignment.getDemande().getId());

        dto.setCandidateId(assignment.getCandidate().getId());

        return dto;
    }
}
