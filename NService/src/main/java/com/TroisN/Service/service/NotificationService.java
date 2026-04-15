package com.TroisN.Service.service;

import com.TroisN.Service.entity.Notification;
import com.TroisN.Service.enums.NotificationRecipientType;
import com.TroisN.Service.event.NotificationCreatedEvent;
import com.TroisN.Service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.TroisN.Service.dto.notification.NotificationResponse;
import com.TroisN.Service.mapper.NotificationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Notification createAndPublish(
            String type,
            String title,
            String message,
            NotificationRecipientType recipientType,
            String recipientKey,
            String targetRoute,
            Long referenceId,
            String referenceType
    ) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRecipientType(recipientType);
        notification.setRecipientKey(recipientKey);
        notification.setTargetRoute(targetRoute);
        notification.setReferenceId(referenceId);
        notification.setReferenceType(referenceType);

        Notification saved = notificationRepository.save(notification);

        eventPublisher.publishEvent(new NotificationCreatedEvent(saved.getId()));

        return saved;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Authentication authentication) {
        String username = authentication.getName();

        return notificationRepository
                .findByRecipientTypeAndRecipientKeyOrderByCreatedAtDesc(
                        NotificationRecipientType.USER_QUEUE,
                        username
                )
                .stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAdminNotifications() {
        return notificationRepository
                .findByRecipientTypeOrderByCreatedAtDesc(NotificationRecipientType.ADMIN_TOPIC)
                .stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @Transactional
    public void markAsRead(Long id, Authentication authentication) {
        String username = authentication.getName();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification introuvable"
                ));

        boolean isUserNotification =
                notification.getRecipientType() == NotificationRecipientType.USER_QUEUE
                        && username.equals(notification.getRecipientKey());

        boolean isAdminNotification =
                notification.getRecipientType() == NotificationRecipientType.ADMIN_TOPIC;

        if (!isUserNotification && !isAdminNotification) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Accès refusé à cette notification"
            );
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllMyNotificationsAsRead(Authentication authentication) {
        String username = authentication.getName();

        List<Notification> notifications = notificationRepository
                .findByRecipientTypeAndRecipientKeyOrderByCreatedAtDesc(
                        NotificationRecipientType.USER_QUEUE,
                        username
                );

        notifications.forEach(notification -> {
            if (!notification.isRead()) {
                notification.setRead(true);
                notification.setReadAt(LocalDateTime.now());
            }
        });

        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void markAllAdminNotificationsAsRead() {
        List<Notification> notifications = notificationRepository
                .findByRecipientTypeOrderByCreatedAtDesc(NotificationRecipientType.ADMIN_TOPIC);

        notifications.forEach(notification -> {
            if (!notification.isRead()) {
                notification.setRead(true);
                notification.setReadAt(LocalDateTime.now());
            }
        });

        notificationRepository.saveAll(notifications);
    }
}
