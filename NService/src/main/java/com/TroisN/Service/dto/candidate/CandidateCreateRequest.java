package com.TroisN.Service.dto.candidate;

import com.TroisN.Service.dto.BaseUserRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CandidateCreateRequest extends BaseUserRequest {


    private String skills;


    private String professional;


    private String cin;


    private String cssNumber;
}
