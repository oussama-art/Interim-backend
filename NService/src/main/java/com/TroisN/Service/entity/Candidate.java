package com.TroisN.Service.entity;

import com.TroisN.Service.enums.CanidateStatus;
import com.TroisN.Service.enums.OfferCandidateStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "candidates")
public class Candidate extends User {


    private String skills;
    private String professional;


    private String cin;


    private String cssNumber;


    private String cvPath;


    private CanidateStatus status;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = CanidateStatus.AVAILABLE;
        }
    }

}
