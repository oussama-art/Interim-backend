package com.TroisN.Service.dto.demande;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class DemandeRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    @Min(1)
    private Integer totalEmployeesNeeded;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private List<DemandeProfilRequest> profils = new ArrayList<>();


    @AssertTrue(message = "La date de fin doit être postérieure ou égale à la date de début")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true; // géré par @NotNull
        }
        return !endDate.isBefore(startDate);
    }
}
