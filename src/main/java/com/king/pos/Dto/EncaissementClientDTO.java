package com.king.pos.Dto;

import com.king.pos.Entitys.ModePaiement;
import com.king.pos.enums.Devise;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EncaissementClientDTO {
    private Long factureId;
    private Double montant;
    private ModePaiement modePaiement;
     private Devise devise; // 🔥
}
