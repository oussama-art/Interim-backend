package com.TroisN.Service.listener;

import com.TroisN.Service.entity.Account;
import com.TroisN.Service.event.AccountRequestCreatedEvent;
import com.TroisN.Service.repository.AccountRepository;
import com.TroisN.Service.repository.AdminRepository;
import com.TroisN.Service.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountNotificationListener {

    private final AccountRepository accountRepository;
    private final AdminRepository adminRepository;
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AccountRequestCreatedEvent event) {

        Account account = accountRepository.findById(event.accountId())
                .orElseThrow(() ->
                        new IllegalStateException("Account not found for notification")
                );

        List<String> adminEmails = adminRepository.findAllAdminEmails();

        if (adminEmails.isEmpty()) {
            return;
        }

        emailService.sendNewAccountRequestNotification(
                adminEmails,
                account.getFirstName() + " " + account.getLastName(),
                account.getCompanyTitle(),
                account.getRequestedAccounts()
        );
    }
}
