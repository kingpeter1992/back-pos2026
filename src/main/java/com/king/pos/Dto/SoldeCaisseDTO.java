package com.king.pos.Dto;

import java.time.LocalDateTime;

import com.king.pos.enums.Devise;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SoldeCaisseDTO {
    private Double soldeActuel;
    private LocalDateTime dateDerniereOperation;
    private Devise devise;

}
