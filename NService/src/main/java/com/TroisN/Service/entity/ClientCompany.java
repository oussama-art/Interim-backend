package com.TroisN.Service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;


@Data
@Entity
@Table(name = "clients")
public class ClientCompany extends User{

    private String title;
    private String description;
    private String sector;

    private Integer nbEmployee;
}
