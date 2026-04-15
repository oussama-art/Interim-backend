package com.TroisN.Service.listener;

import com.TroisN.Service.dto.notification.NotificationMessage;
import com.TroisN.Service.entity.Notification;
import com.TroisN.Service.enums.NotificationRecipientType;
import com.TroisN.Service.enums.NotificationStatus;
import com.TroisN.Service.event.NotificationCreatedEvent;
import com.TroisN.Service.repository.NotificationRepository;
import com.TroisN.Service.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationCreatedListener {

    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationCreatedEvent event) {

        Notification notification = notificationRepository.findById(event.notificationId())
                .orElseThrow(() -> new IllegalArgumentException("Notification introuvable"));

        Long clientId = null;
        Long offerId = null;
        Long demandeId = null;

        if ("CLIENT".equalsIgnoreCase(notification.getReferenceType())) {
            clientId = notification.getReferenceId();
        } else if ("OFFER".equalsIgnoreCase(notification.getReferenceType())) {
            offerId = notification.getReferenceId();
        } else if ("DEMANDE".equalsIgnoreCase(notification.getReferenceType())) {
            demandeId = notification.getReferenceId();
        }

        NotificationMessage payload = new NotificationMessage(
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getTargetRoute(),
                clientId,
                offerId,
                demandeId,
                notification.getCreatedAt()
        );

        try {
            if (notification.getRecipientType() == NotificationRecipientType.ADMIN_TOPIC) {

                String recipientKey = notification.getRecipientKey();

                if ("ADMIN_ACCOUNT_REQUESTS".equals(recipientKey)) {
                    webSocketNotificationService.notifyAdminsAccountRequests(payload);
                } else if ("ADMIN_OFFERS".equals(recipientKey)) {
                    webSocketNotificationService.notifyAdminsOffers(payload);
                } else if ("ADMIN_DEMANDES".equals(recipientKey)) {
                    webSocketNotificationService.notifyAdminsDemandes(payload);
                } else {
                    webSocketNotificationService.notifyAdminsDemandes(payload);
                }

            } else if (notification.getRecipientType() == NotificationRecipientType.USER_QUEUE) {
                webSocketNotificationService.notifyClientByUsername(
                        notification.getRecipientKey(),
                        payload
                );

            } else if (notification.getRecipientType() == NotificationRecipientType.CLIENT_TOPIC) {
                Long recipientClientId = Long.valueOf(notification.getRecipientKey());
                payload.setClientId(recipientClientId);

                webSocketNotificationService.notifyClientByClientId(recipientClientId, payload);
            }

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

        } catch (Exception ex) {
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
        }
    }
}