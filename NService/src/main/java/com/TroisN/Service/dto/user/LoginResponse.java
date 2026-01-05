package com.TroisN.Service.dto.user;

import com.TroisN.Service.dto.BaseUserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {


    private BaseUserResponse details;
}

