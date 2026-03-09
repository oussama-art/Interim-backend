package com.TroisN.Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatedAccountInfo {
    private String email;
    private String password;
}
