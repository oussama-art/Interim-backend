package com.TroisN.Service.dto.notification;


import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        String targetRoute,
        Long referenceId,
        String referenceType,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        String status
) {
}