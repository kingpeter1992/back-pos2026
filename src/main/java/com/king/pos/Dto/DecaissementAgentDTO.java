package com.king.pos.Dto;

import com.king.pos.Entitys.Categorie;
import com.king.pos.Entitys.ModePaiement;
import com.king.pos.enums.Devise;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
public class DecaissementAgentDTO {
    private Long agentId;
    private Double montant;
    private Categorie categorie; // PRET, AVANCE, SALAIRE
    private ModePaiement modePaiement;
     private Devise devise; // 🔥
}

