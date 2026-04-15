package com.TroisN.Service.dto.notification;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@Getter
public class NotificationMessage {

    private String type;
    private String title;
    private String message;

    private String targetRoute;

    private Long clientId;
    private Long offerId;
    private Long demandeId;

    private LocalDateTime createdAt;

    public NotificationMessage() {}

    public NotificationMessage(
            String type,
            String title,
            String message,
            String targetRoute,
            Long clientId,
            Long offerId,
            Long demandeId,
            LocalDateTime createdAt
    ) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.targetRoute = targetRoute;
        this.clientId = clientId;
        this.offerId = offerId;
        this.demandeId = demandeId;
        this.createdAt = createdAt;
    }
}