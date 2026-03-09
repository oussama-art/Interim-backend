package com.TroisN.Service.dto.admin;

import com.TroisN.Service.dto.BaseUserRequest;
import com.TroisN.Service.dto.BaseUserResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminCreateRequest extends BaseUserRequest {


    @NotNull
    @Min(0)
    private Integer experienceYear;

    @NotNull
    @Min(0)
    private Integer adminLevel;


}
