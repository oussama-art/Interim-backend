package com.TroisN.Service.service;

import com.TroisN.Service.dto.CreatedAccountInfo;
import com.TroisN.Service.dto.account.AccountCreationRequest;
import com.TroisN.Service.dto.account.AccountCreationResponse;
import com.TroisN.Service.dto.account.EmailCheckResponse;
import com.TroisN.Service.dto.client.ClientCreateRequest;
import com.TroisN.Service.entity.Account;
import com.TroisN.Service.entity.AccountEmail;
import com.TroisN.Service.enums.RequestStatus;
import com.TroisN.Service.event.AccountRequestCreatedEvent;
import com.TroisN.Service.mapper.AccountCreationMapper;
import com.TroisN.Service.repository.AccountRepository;
import com.TroisN.Service.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import org.springframework.context.ApplicationEventPublisher;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final ClientService clientService;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;



    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}";
    private static final String ALL = LOWER + UPPER + DIGITS + SPECIAL;


    @Transactional
    public AccountCreationResponse create(AccountCreationRequest dto) {

        Account entity = AccountCreationMapper.toEntity(dto);
        Account saved = accountRepository.save(entity);

        eventPublisher.publishEvent(
                new AccountRequestCreatedEvent(saved.getId())
        );

        return AccountCreationMapper.toResponse(saved);
    }


    public EmailCheckResponse checkEmailExists(String email) {

        boolean exists = clientRepository.existsByEmailAddress(email);

        return new EmailCheckResponse(exists);
    }



    @Transactional(readOnly = true)
    public List<AccountCreationResponse> getAll() {
        return accountRepository.findAllWithEmails()
                .stream()
                .map(AccountCreationMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public AccountCreationResponse getById(Long id) {

        Account account = accountRepository.findWithEmailsById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account creation request not found"
                ));

        return AccountCreationMapper.toResponse(account);
    }



    public AccountCreationResponse update(Long id, AccountCreationRequest dto) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account creation request not found"
                ));

        account.setFirstName(dto.getFirstName());
        account.setLastName(dto.getLastName());
        account.setPhoneNumber(dto.getPhoneNumber());
        account.setExperienceYear(dto.getExperienceYear());
        account.setCompanyTitle(dto.getCompanyTitle());
        account.setCompanyDescription(dto.getCompanyDescription());
        account.setSector(dto.getSector());
        account.setNbEmployee(dto.getNbEmployee());
        account.setRequestedAccounts(dto.getRequestedAccounts());

        return AccountCreationMapper.toResponse(
                accountRepository.save(account)
        );
    }


    public void delete(Long id) {

        if (!accountRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Account creation request not found"
            );
        }

        accountRepository.deleteById(id);
    }

    @Transactional
    public List<CreatedAccountInfo> approve(Long id, List<String> selectedEmails)
    {


        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account creation request not found"
                ));




        List<String> allowedEmails = account.getEmails()
                .stream()
                .map(AccountEmail::getEmail)
                .toList();

        for (String email : selectedEmails) {
            if (!allowedEmails.contains(email)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Email not allowed: " + email
                );
            }
        }

        account.getEmails().forEach(accountEmail -> {
            if (selectedEmails.contains(accountEmail.getEmail())
                    && accountEmail.getStatus() == RequestStatus.PENDING) {
                accountEmail.setStatus(RequestStatus.APPROVED);
            }
        });

        boolean hasApproved = account.getEmails().stream()
                .anyMatch(e -> e.getStatus() == RequestStatus.APPROVED);

        boolean allRejected = account.getEmails().stream()
                .allMatch(e -> e.getStatus() == RequestStatus.REJECTED);

        if (allRejected) {
            account.setStatus(RequestStatus.REJECTED);
            account.setRejectedAt(LocalDateTime.now());
        } else if (hasApproved) {
            account.setStatus(RequestStatus.APPROVED);
            account.setValidatedAt(LocalDateTime.now());
        } else {
            account.setStatus(RequestStatus.PENDING);
        }

        accountRepository.save(account);


        ClientCreateRequest baseDto = mapToClientCreateRequest(account);
        List<CreatedAccountInfo> createdAccounts = new ArrayList<>();

        for (AccountEmail accountEmail : account.getEmails()) {

            if (accountEmail.getStatus() != RequestStatus.APPROVED) {
                continue;
            }

            String email = accountEmail.getEmail();

            if (clientRepository.existsByEmailAddress(email)) {
                continue;
            }

            String password = generateStrongPassword();

            ClientCreateRequest clientDto = new ClientCreateRequest();
            clientDto.setFirstName(baseDto.getFirstName());
            clientDto.setLastName(baseDto.getLastName());
            clientDto.setPhoneNumber(baseDto.getPhoneNumber());
            clientDto.setExperienceYear(baseDto.getExperienceYear());
            clientDto.setTitle(baseDto.getTitle());
            clientDto.setDescription(baseDto.getDescription());
            clientDto.setSector(baseDto.getSector());
            clientDto.setNbEmployee(baseDto.getNbEmployee());

            clientDto.setEmailAddress(email);
            clientDto.setPassword(password);
            clientDto.setConfirmPassword(password);
            clientDto.setNumDemande(account.getId());

            clientService.createClient(clientDto);

            createdAccounts.add(new CreatedAccountInfo(email, password));
        }


        if (!createdAccounts.isEmpty()) {
            emailService.sendAccountsCreatedEmail(
                    account.getEmailAddress(),
                    createdAccounts
            );
        }

        return createdAccounts;

    }



    @Transactional
    public void reject(Long id, String reason) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account creation request not found"
                ));

        if (account.getStatus() != RequestStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only pending account requests can be rejected"
            );
        }

        account.setStatus(RequestStatus.REJECTED);
        account.setRejectedAt(LocalDateTime.now());
        account.setRejectionReason(reason);


        account.getEmails().forEach(email ->
                email.setStatus(RequestStatus.REJECTED)
        );

        accountRepository.save(account);
    }







    private String generateStrongPassword() {

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        for (int i = 4; i < 10; i++) {
            password.append(ALL.charAt(random.nextInt(ALL.length())));
        }

        return shuffle(password.toString(), random);
    }

    private String shuffle(String input, SecureRandom random) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }


    private ClientCreateRequest mapToClientCreateRequest(Account account) {

        ClientCreateRequest dto = new ClientCreateRequest();

        dto.setFirstName(account.getFirstName());
        dto.setLastName(account.getLastName());
        dto.setPhoneNumber(account.getPhoneNumber());
        dto.setExperienceYear(account.getExperienceYear());

        dto.setTitle(account.getCompanyTitle());
        dto.setDescription(account.getCompanyDescription());
        dto.setSector(account.getSector());
        dto.setNbEmployee(account.getNbEmployee());
        dto.setNumberOfAccounts(account.getRequestedAccounts());

        return dto;
    }




}
