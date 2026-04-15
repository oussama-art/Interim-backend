package com.TroisN.Service.repository;

import com.TroisN.Service.entity.Notification;
import com.TroisN.Service.enums.NotificationRecipientType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientTypeOrderByCreatedAtDesc(NotificationRecipientType recipientType);

    List<Notification> findByRecipientKeyOrderByCreatedAtDesc(String recipientKey);

    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();

    List<Notification> findByRecipientTypeAndRecipientKeyOrderByCreatedAtDesc(
            NotificationRecipientType recipientType,
            String recipientKey
    );
}