package com.TroisN.Service.controller;

import com.TroisN.Service.dto.assignment.AssignmentCreateRequest;
import com.TroisN.Service.dto.assignment.AssignmentResponse;
import com.TroisN.Service.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }


    @PostMapping("/accept")
    public ResponseEntity<AssignmentResponse> acceptCandidate(
            @Valid @RequestBody AssignmentCreateRequest request
    ) {
        AssignmentResponse response = assignmentService.acceptCandidate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping
    public ResponseEntity<Page<AssignmentResponse>> getAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                assignmentService.getAssignments(pageable)
        );
    }

}
