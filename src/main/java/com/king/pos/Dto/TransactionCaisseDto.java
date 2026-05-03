package com.king.pos.Dto;

import java.time.LocalDateTime;

import com.king.pos.Entitys.ModePaiement;
import com.king.pos.Entitys.TypeTransaction;
import com.king.pos.enums.Devise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCaisseDto {

    private Long id;

    private LocalDateTime dateTransaction;

    private String reference;

    private TypeTransaction type;

    private Devise devise;

    private double montant;

    private String category;

    private ModePaiement modePaiement;

    private String description;

    private double soldeAvant;

    private double soldeApres;

    private String sens;

    private String userId;

    private Double tauxChange;

    private Double montantConvertiUSD;

    private Double montantConvertiCDF;
}