package com.king.pos.Dto;

import com.king.pos.Entitys.ModePaiement;
import com.king.pos.Entitys.TypeTransaction;
import com.king.pos.enums.Devise;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationCaisseDTO {

    private TypeTransaction type;

    private Devise devise;

    private double montant;

    private String category;

    private ModePaiement modePaiement;

    private String description;

    private String reference;

    private Double tauxChange;
}