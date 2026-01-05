package com.TroisN.Service.controller;

import com.TroisN.Service.dto.assignment.AssignmentResponse;
import com.TroisN.Service.dto.offer.OfferAcceptRequest;
import com.TroisN.Service.dto.offer.OfferCreateRequest;
import com.TroisN.Service.dto.offer.OfferResponse;
import com.TroisN.Service.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<OfferResponse> createOffer(
            @PathVariable Long clientId,
            @Valid @RequestBody OfferCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(offerService.createOffer(clientId, request));
    }

    @PostMapping("/{offerId}/accept")
    public ResponseEntity<AssignmentResponse> acceptOffer(
            @PathVariable Long clientId,
            @PathVariable Long offerId,
            @Valid @RequestBody OfferAcceptRequest request
    ) {

        AssignmentResponse response = offerService.acceptOffer(
                offerId,
                request.getCandidateId(),
                clientId,
                request.getStartDate(),
                request.getEndDate()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{offerId}")
    public ResponseEntity<OfferResponse> getOfferById(
            @PathVariable Long offerId
    ) {
        return ResponseEntity.ok(
                offerService.getOfferById(offerId)
        );
    }

    @GetMapping
    public ResponseEntity<List<OfferResponse>> getOffersByClient(
            @PathVariable Long clientId
    ) {
        return ResponseEntity.ok(
                offerService.getOffersByClientId(clientId)
        );
    }
}
