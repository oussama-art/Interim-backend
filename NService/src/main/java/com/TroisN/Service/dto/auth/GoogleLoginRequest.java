package com.TroisN.Service.dto.auth;

import com.TroisN.Service.enums.RoleType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {

    @NotNull
    private RoleType role;
}
