package com.TroisN.Service.mapper;

import com.TroisN.Service.dto.notification.NotificationResponse;
import com.TroisN.Service.entity.Notification;

public class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getTargetRoute(),
                notification.getReferenceId(),
                notification.getReferenceType(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt(),
                notification.getStatus().name()
        );
    }
}