package com.TroisN.Service.controller;
import com.TroisN.Service.dto.demande.DemandeRequest;
import com.TroisN.Service.dto.demande.DemandeResponse;
import com.TroisN.Service.service.DemandeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demandes")
public class DemandeController {

    private final DemandeService demandeService;

    public DemandeController(DemandeService demandeService) {
        this.demandeService = demandeService;
    }

    @PostMapping("/create")
    public DemandeResponse createDemande(
            Authentication authentication,
            @Valid @RequestBody DemandeRequest request
    ) {
        return demandeService.createDemande(authentication, request);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public Page<DemandeResponse> getAllDemadandes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return demandeService.getAllDemandes(page, size);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PatchMapping("/demande")
    public DemandeResponse updateDemande(
        Authentication authentication,
        @RequestParam Long demandeId,
        @Valid @RequestBody  DemandeRequest demande
    )    {
        return demandeService.updateDemande(authentication, demandeId, demande);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/my-demandes/detail")
    public DemandeResponse getDemandeDetail(
            Authentication authentication,
            @RequestParam Long demandeId
    ) {
        return demandeService.getDemandeByIdAndClient(authentication, demandeId);
    }

}
