package com.TroisN.Service.service;

import com.TroisN.Service.dto.notification.NotificationMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyClientByUsername(String username, NotificationMessage notification) {
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                notification
        );
    }

    public void notifyClientByClientId(Long clientId, NotificationMessage notification) {
        messagingTemplate.convertAndSend(
                "/topic/client/" + clientId + "/notifications",
                notification
        );
    }

    public void notifyAdminsAccountRequests(NotificationMessage notification) {
        messagingTemplate.convertAndSend(
                "/topic/admin/account-requests",
                notification
        );
    }

    public void notifyAdminsOffers(NotificationMessage notification) {
        messagingTemplate.convertAndSend(
                "/topic/admin/offers",
                notification
        );
    }

    public void notifyAdminsDemandes(NotificationMessage notification) {
        messagingTemplate.convertAndSend(
                "/topic/admin/demandes",
                notification
        );
    }
}