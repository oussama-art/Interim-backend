package com.TroisN.Service.controller;

import com.TroisN.Service.dto.notification.NotificationResponse;
import com.TroisN.Service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationQueryService;

    @GetMapping("/me")
    public List<NotificationResponse> getMyNotifications(Authentication authentication) {
        return notificationQueryService.getMyNotifications(authentication);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<NotificationResponse> getAdminNotifications() {
        return notificationQueryService.getAdminNotifications();
    }

    @PatchMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id, Authentication authentication) {
        notificationQueryService.markAsRead(id, authentication);
    }

    @PatchMapping("/me/read-all")
    public void markAllMyNotificationsAsRead(Authentication authentication) {
        notificationQueryService.markAllMyNotificationsAsRead(authentication);
    }

    @PatchMapping("/admin/read-all")
    @PreAuthorize("hasRole('ADMIN')")
    public void markAllAdminNotificationsAsRead() {
        notificationQueryService.markAllAdminNotificationsAsRead();
    }
}