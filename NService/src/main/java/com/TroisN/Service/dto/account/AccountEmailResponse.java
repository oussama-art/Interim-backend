package com.TroisN.Service.dto.account;

import com.TroisN.Service.enums.RequestStatus;
import lombok.Data;

@Data
public class AccountEmailResponse {

    private String email;
    private RequestStatus status;
}
