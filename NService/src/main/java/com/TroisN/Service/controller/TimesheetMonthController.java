package com.TroisN.Service.controller;

import com.TroisN.Service.dto.timesheetmonth.TimesheetMonthCreateRequest;
import com.TroisN.Service.dto.timesheetmonth.TimesheetMonthResponse;
import com.TroisN.Service.dto.timesheetmonth.TimesheetMonthUpdateRequest;
import com.TroisN.Service.service.TimesheetMonthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/timesheet-months")
public class TimesheetMonthController {

    private final TimesheetMonthService service;


    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimesheetMonthResponse create(@Valid @RequestBody TimesheetMonthCreateRequest request) {
        return service.create(request);
    }


    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    @GetMapping("/{id}")
    public TimesheetMonthResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }


    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    @GetMapping("/candidates/{candidateId}")
    public List<TimesheetMonthResponse> listByCandidate(@PathVariable Long candidateId) {
        return service.listByCandidate(candidateId);
    }


    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    @GetMapping("/candidates/{candidateId}/page")
    public Page<TimesheetMonthResponse> pageByCandidate(
            @PathVariable Long candidateId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return service.pageByCandidate(candidateId, pageable);
    }


    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    @PutMapping("/{id}")
    public TimesheetMonthResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TimesheetMonthUpdateRequest request
    ) {
        return service.update(id, request);
    }


    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}