package com.TroisN.Service.entity;

import com.TroisN.Service.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Embeddable
public class AccountEmail {

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
}
