package com.king.pos.Dto;

import com.king.pos.Entitys.ModePaiement;
import com.king.pos.enums.Devise;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EncaissementAgentDTO {
    private Long agentId;
    private Double montant;
    private String motif;
    private ModePaiement modePaiement;
    private Devise devise;
}
